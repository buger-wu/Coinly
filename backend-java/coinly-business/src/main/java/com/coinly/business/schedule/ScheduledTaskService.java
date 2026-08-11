package com.coinly.business.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coinly.business.book.entity.BookEntity;
import com.coinly.business.book.mapper.BookMapper;
import com.coinly.business.statistics.entity.MonthlySnapshotEntity;
import com.coinly.business.statistics.mapper.MonthlySnapshotMapper;
import com.coinly.business.transaction.entity.RecurringTransactionEntity;
import com.coinly.business.transaction.entity.TransactionEntity;
import com.coinly.business.transaction.mapper.RecurringTransactionMapper;
import com.coinly.business.transaction.mapper.TransactionMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * V9.1: 定时任务服务。
 * 1. 周期记账：每天扫描需要执行的周期配置并生成交易。
 * 2. 月账单：每月1号生成上月账单快照。
 */
@Component
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RecurringTransactionMapper recurringTransactionMapper;
    private final TransactionMapper transactionMapper;
    private final BookMapper bookMapper;
    private final MonthlySnapshotMapper monthlySnapshotMapper;

    public ScheduledTaskService(RecurringTransactionMapper recurringTransactionMapper,
                                TransactionMapper transactionMapper,
                                BookMapper bookMapper,
                                MonthlySnapshotMapper monthlySnapshotMapper) {
        this.recurringTransactionMapper = recurringTransactionMapper;
        this.transactionMapper = transactionMapper;
        this.bookMapper = bookMapper;
        this.monthlySnapshotMapper = monthlySnapshotMapper;
    }

    /**
     * 每天 01:00 执行周期记账。
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void recurringTransactionJob() {
        LocalDate today = LocalDate.now();
        log.info("[周期记账任务] 开始执行，日期: {}", today);

        List<RecurringTransactionEntity> list = recurringTransactionMapper.selectList(
                new LambdaQueryWrapper<RecurringTransactionEntity>()
                        .eq(RecurringTransactionEntity::getStatus, 1)
                        .le(RecurringTransactionEntity::getNextExecuteDate, today)
        );

        int count = 0;
        for (RecurringTransactionEntity config : list) {
            try {
                TransactionEntity tx = new TransactionEntity();
                tx.setUserId(config.getUserId());
                tx.setBookId(config.getBookId());
                tx.setCategoryId(config.getCategoryId());
                tx.setType(config.getType());
                tx.setAmount(config.getAmount());
                tx.setRemark(config.getRemark());
                tx.setTransactionDate(today);
                tx.setDeleted(0);
                tx.setCreateTime(LocalDateTime.now());
                tx.setUpdateTime(LocalDateTime.now());
                transactionMapper.insert(tx);

                // 更新下次执行日期
                LocalDate nextDate = calculateNextDate(config.getCycleType(), config.getCycleDay(), today);
                config.setNextExecuteDate(nextDate);
                config.setUpdateTime(LocalDateTime.now());
                recurringTransactionMapper.updateById(config);

                count++;
            } catch (Exception e) {
                log.error("[周期记账任务] 处理配置 {} 失败: {}", config.getId(), e.getMessage(), e);
            }
        }

        log.info("[周期记账任务] 完成，生成 {} 条交易", count);
    }

    /**
     * 每月1号 02:00 生成上月账单快照。
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void monthlySnapshotJob() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate startDate = lastMonth.atDay(1);
        LocalDate endDate = lastMonth.atEndOfMonth();
        String month = lastMonth.toString();
        log.info("[月账单任务] 开始生成 {} 月账单快照", month);

        List<BookEntity> books = bookMapper.selectList(
                new LambdaQueryWrapper<BookEntity>()
                        .eq(BookEntity::getDeleted, 0)
        );

        int count = 0;
        for (BookEntity book : books) {
            try {
                generateSnapshot(book.getUserId(), book.getId(), month, startDate, endDate);
                count++;
            } catch (Exception e) {
                log.error("[月账单任务] 生成账本 {} 快照失败: {}", book.getId(), e.getMessage(), e);
            }
        }

        log.info("[月账单任务] 完成，生成 {} 条快照", count);
    }

    private void generateSnapshot(Long userId, Long bookId, String month,
                                   LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<TransactionEntity> wrapper = new LambdaQueryWrapper<TransactionEntity>()
                .eq(TransactionEntity::getUserId, userId)
                .eq(TransactionEntity::getBookId, bookId)
                .eq(TransactionEntity::getDeleted, 0)
                .ge(TransactionEntity::getTransactionDate, startDate)
                .le(TransactionEntity::getTransactionDate, endDate);

        List<TransactionEntity> transactions = transactionMapper.selectList(wrapper);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryMap = new HashMap<>();

        for (TransactionEntity tx : transactions) {
            if (tx.getType() != null && tx.getType() == 1) {
                totalIncome = totalIncome.add(tx.getAmount());
            } else {
                totalExpense = totalExpense.add(tx.getAmount());
            }
            String key = tx.getType() + "_" + tx.getCategoryId();
            categoryMap.merge(key, tx.getAmount(), BigDecimal::add);
        }

        MonthlySnapshotEntity snapshot = new MonthlySnapshotEntity();
        snapshot.setUserId(userId);
        snapshot.setBookId(bookId);
        snapshot.setSnapshotMonth(month);
        snapshot.setTotalIncome(totalIncome);
        snapshot.setTotalExpense(totalExpense);
        snapshot.setNetAmount(totalIncome.subtract(totalExpense));
        snapshot.setTransactionCount(transactions.size());
        snapshot.setCategorySummary(toJson(categoryMap));
        snapshot.setCreateTime(LocalDateTime.now());
        snapshot.setUpdateTime(LocalDateTime.now());

        // 覆盖已存在的快照
        monthlySnapshotMapper.delete(
                new LambdaQueryWrapper<MonthlySnapshotEntity>()
                        .eq(MonthlySnapshotEntity::getUserId, userId)
                        .eq(MonthlySnapshotEntity::getBookId, bookId)
                        .eq(MonthlySnapshotEntity::getSnapshotMonth, month)
        );
        monthlySnapshotMapper.insert(snapshot);
    }

    private LocalDate calculateNextDate(String cycleType, Integer cycleDay, LocalDate baseDate) {
        return switch (cycleType) {
            case "daily" -> baseDate.plusDays(1);
            case "weekly" -> baseDate.plusWeeks(1);
            case "monthly" -> baseDate.plusMonths(1);
            case "yearly" -> baseDate.plusYears(1);
            default -> baseDate.plusMonths(1);
        };
    }

    /**
     * 用 Jackson 把 Map 序列化为 JSON 字符串（替代 fastjson2）。
     */
    private String toJson(Map<String, BigDecimal> map) {
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("[toJson] 序列化失败，返回空对象: {}", e.getMessage());
            return "{}";
        }
    }
}
