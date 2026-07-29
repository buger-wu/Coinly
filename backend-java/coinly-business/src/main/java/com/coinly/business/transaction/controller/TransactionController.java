package com.coinly.business.transaction.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coinly.business.book.entity.BookEntity;
import com.coinly.business.book.service.BookService;
import com.coinly.business.category.entity.CategoryEntity;
import com.coinly.business.category.service.CategoryService;
import com.coinly.business.transaction.dto.CreateTransactionRequest;
import com.coinly.business.transaction.dto.TransactionQueryRequest;
import com.coinly.business.transaction.dto.UpdateTransactionRequest;
import com.coinly.business.transaction.entity.TransactionEntity;
import com.coinly.business.transaction.service.TransactionService;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import com.coinly.common.domain.PageResponse;
import com.coinly.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/books/{bookId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final BookService bookService;
    private final CategoryService categoryService;

    public TransactionController(TransactionService transactionService, BookService bookService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

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
        return CommonResponse.success(transaction);
    }

    @GetMapping
    public CommonResponse<PageResponse<TransactionEntity>> getTransactionList(@PathVariable Long bookId,
                                                                               @RequestParam(defaultValue = "1") int page,
                                                                               @RequestParam(defaultValue = "20") int size,
                                                                               TransactionQueryRequest query) {
        Long userId = UserContext.getUserId();
        Page<TransactionEntity> result = transactionService.getTransactionPage(userId, bookId, query, page, size);
        return CommonResponse.success(PageResponse.of(result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/{id}")
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
        return CommonResponse.success(transaction);
    }

    @PutMapping("/{id}")
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
        return CommonResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
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
        return CommonResponse.success("删除成功", null);
    }
}