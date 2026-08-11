package com.coinly.business.transaction.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coinly.business.book.entity.BookEntity;
import com.coinly.business.book.service.BookService;
import com.coinly.business.cache.StatisticsCacheService;
import com.coinly.business.category.entity.CategoryEntity;
import com.coinly.business.category.service.CategoryService;
import com.coinly.business.mq.dto.ReportMessage;
import com.coinly.business.mq.producer.MessageProducer;
import com.coinly.business.transaction.dto.CreateTransactionRequest;
import com.coinly.business.transaction.dto.TransactionExcelDTO;
import com.coinly.business.transaction.dto.TransactionQueryRequest;
import com.coinly.business.transaction.dto.UpdateTransactionRequest;
import com.coinly.business.transaction.entity.TransactionEntity;
import com.coinly.business.transaction.service.TransactionService;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import com.coinly.common.domain.PageResponse;
import com.coinly.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "交易模块")
@RestController
@RequestMapping("/v1/books/{bookId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final MessageProducer messageProducer;
    private final StatisticsCacheService cacheService;

    public TransactionController(TransactionService transactionService, BookService bookService,
                                 CategoryService categoryService, MessageProducer messageProducer,
                                 StatisticsCacheService cacheService) {
        this.transactionService = transactionService;
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.messageProducer = messageProducer;
        this.cacheService = cacheService;
    }

    @Operation(summary = "新增交易")
    @PostMapping
    public CommonResponse<TransactionEntity> createTransaction(@PathVariable Long bookId,
                                                               @Valid @RequestBody CreateTransactionRequest request) {
        Long userId = UserContext.getUserId();

        BookEntity book = bookService.lambdaQuery()
                .eq(BookEntity::getId, bookId)
                .eq(BookEntity::getUserId, userId)
                .one();
        if (book == null) {
            throw new BusinessException("账本不存在");
        }

        CategoryEntity category = categoryService.lambdaQuery()
                .eq(CategoryEntity::getId, request.getCategoryId())
                .eq(CategoryEntity::getUserId, userId)
                .one();
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        TransactionEntity transaction = new TransactionEntity();
        transaction.setUserId(userId);
        transaction.setBookId(bookId);
        transaction.setCategoryId(request.getCategoryId());
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setRemark(request.getRemark());
        transaction.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());

        transactionService.save(transaction);

        // V7: 记账后发 MQ 消息（异步生成月度快照）+ 清除统计缓存
        String month = transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        messageProducer.sendReportMessage(new ReportMessage(
                userId, bookId, transaction.getId(), month, System.currentTimeMillis()
        ));
        cacheService.evictStats(userId, month);

        // V9: 填充 categoryName 后返回
        transaction.setCategoryName(category.getName());
        return CommonResponse.success(transaction);
    }

    @Operation(summary = "交易列表（分页+筛选）")
    @GetMapping
    public CommonResponse<PageResponse<TransactionEntity>> getTransactionList(@PathVariable Long bookId,
                                                                               @RequestParam(defaultValue = "1") int page,
                                                                               @RequestParam(defaultValue = "20") int size,
                                                                               TransactionQueryRequest query) {
        Long userId = UserContext.getUserId();
        Page<TransactionEntity> result = transactionService.getTransactionPage(userId, bookId, query, page, size);
        fillCategoryNames(result.getRecords(), userId);
        return CommonResponse.success(PageResponse.of(result.getRecords(), result.getTotal(), page, size));
    }

    @Operation(summary = "导出 Excel")
    @GetMapping("/export")
    public void exportExcel(@PathVariable Long bookId, TransactionQueryRequest query,
                            HttpServletResponse response) throws IOException {
        Long userId = UserContext.getUserId();

        // 校验账本归属
        BookEntity book = bookService.lambdaQuery()
                .eq(BookEntity::getId, bookId)
                .eq(BookEntity::getUserId, userId)
                .one();
        if (book == null) {
            throw new BusinessException("账本不存在");
        }

        List<TransactionExcelDTO> data = transactionService.listForExport(userId, bookId, query);

        // 设置响应头：Excel 文件 + UTF-8 文件名
        String fileName = URLEncoder.encode("交易记录_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        EasyExcel.write(response.getOutputStream(), TransactionExcelDTO.class)
                .sheet("交易记录")
                .doWrite(data);
    }

    @Operation(summary = "交易详情")
    @GetMapping("/{id:\\d+}")
    public CommonResponse<TransactionEntity> getTransactionById(@PathVariable Long bookId, @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        TransactionEntity transaction = transactionService.lambdaQuery()
                .eq(TransactionEntity::getId, id)
                .eq(TransactionEntity::getBookId, bookId)
                .eq(TransactionEntity::getUserId, userId)
                .one();

        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }
        fillCategoryNames(List.of(transaction), userId);
        return CommonResponse.success(transaction);
    }

    /**
     * 批量填充交易记录的分类名称。
     */
    private void fillCategoryNames(List<TransactionEntity> transactions, Long userId) {
        if (transactions == null || transactions.isEmpty()) return;
        List<Long> categoryIds = transactions.stream()
                .map(TransactionEntity::getCategoryId)
                .filter(cid -> cid != null)
                .distinct()
                .collect(Collectors.toList());
        if (categoryIds.isEmpty()) return;
        Map<Long, String> categoryMap = categoryService.lambdaQuery()
                .eq(CategoryEntity::getUserId, userId)
                .in(CategoryEntity::getId, categoryIds)
                .list()
                .stream()
                .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));
        transactions.forEach(t -> t.setCategoryName(categoryMap.get(t.getCategoryId())));
    }

    @Operation(summary = "编辑交易")
    @PutMapping("/{id:\\d+}")
    public CommonResponse<Void> updateTransaction(@PathVariable Long bookId, @PathVariable Long id,
                                                  @Valid @RequestBody UpdateTransactionRequest request) {
        Long userId = UserContext.getUserId();
        TransactionEntity transaction = transactionService.lambdaQuery()
                .eq(TransactionEntity::getId, id)
                .eq(TransactionEntity::getBookId, bookId)
                .eq(TransactionEntity::getUserId, userId)
                .one();

        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        // 校验新分类归属（如果 categoryId 有变更）
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryService.lambdaQuery()
                    .eq(CategoryEntity::getId, request.getCategoryId())
                    .eq(CategoryEntity::getUserId, userId)
                    .one();
            if (category == null) {
                throw new BusinessException("分类不存在");
            }
        }

        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getCategoryId() != null) {
            transaction.setCategoryId(request.getCategoryId());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getRemark() != null) {
            transaction.setRemark(request.getRemark());
        }
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }
        transaction.setUpdateTime(LocalDateTime.now());

        transactionService.updateById(transaction);

        // V7: 清除统计缓存
        String month = transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        cacheService.evictStats(userId, month);

        return CommonResponse.success("修改成功", null);
    }

    @Operation(summary = "删除交易")
    @DeleteMapping("/{id:\\d+}")
    public CommonResponse<Void> deleteTransaction(@PathVariable Long bookId, @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        TransactionEntity transaction = transactionService.lambdaQuery()
                .eq(TransactionEntity::getId, id)
                .eq(TransactionEntity::getBookId, bookId)
                .eq(TransactionEntity::getUserId, userId)
                .one();

        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        transactionService.removeById(id);

        // V7: 清除统计缓存
        String month = transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        cacheService.evictStats(userId, month);

        return CommonResponse.success("删除成功", null);
    }
}