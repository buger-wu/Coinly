package com.coinly.business.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coinly.business.transaction.entity.TransactionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TransactionMapper extends BaseMapper<TransactionEntity> {

    /**
     * 按交易类型汇总金额（用于月度收支总览）。
     *
     * @param userId    用户 ID
     * @param bookId    账本 ID，null 表示所有账本
     * @param type      交易类型：0=支出，1=收入
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @return 金额合计，无数据时为 null
     */
    @Select("<script>" +
            "SELECT SUM(amount) FROM biz_transaction WHERE user_id = #{userId} AND type = #{type} " +
            "AND transaction_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 " +
            "<if test='bookId != null'>AND book_id = #{bookId}</if>" +
            "</script>")
    BigDecimal sumByType(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("type") Integer type,
                         @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按分类汇总支出金额（用于分类占比统计）。
     *
     * <p>只统计 type=0（支出），关联 biz_category 获取分类名称。
     * 结果按金额倒序排列，便于前端直接展示。
     *
     * @param userId    用户 ID
     * @param bookId    账本 ID，null 表示所有账本
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @return 分类名称 + 金额合计列表
     */
    @Select("<script>" +
            "SELECT c.name, SUM(t.amount) as total_amount FROM biz_transaction t " +
            "LEFT JOIN biz_category c ON t.category_id = c.id " +
            "WHERE t.user_id = #{userId} AND t.type = 0 " +
            "AND t.transaction_date BETWEEN #{startDate} AND #{endDate} AND t.deleted = 0 " +
            "<if test='bookId != null'>AND t.book_id = #{bookId}</if>" +
            "GROUP BY t.category_id, c.name ORDER BY total_amount DESC" +
            "</script>")
    List<CategoryAmountDTO> sumByCategory(@Param("userId") Long userId, @Param("bookId") Long bookId,
                                          @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 分类金额汇总 DTO（用于 {@link #sumByCategory} 返回值）。
     *
     * <p>使用 Java 16+ record，字段自动映射 SQL 结果：
     * {@code c.name -> name}，{@code SUM(t.amount) as total_amount -> totalAmount}。
     *
     * @param name        分类名称
     * @param totalAmount 金额合计
     */
    record CategoryAmountDTO(String name, BigDecimal totalAmount) {}

    /**
     * 按账本汇总收支余额（用于账本余额汇总接口）。
     *
     * <p>一条 SQL 查出用户所有账本的总收入、总支出，余额 = 收入 - 支出。
     * 统计全部历史数据（非按月），排除已删除记录。
     *
     * @param userId 用户 ID
     * @return 账本 ID + 账本名称 + 总收入 + 总支出列表
     */
    @Select("SELECT b.id as book_id, b.name as book_name, " +
            "COALESCE(SUM(CASE WHEN t.type = 1 THEN t.amount ELSE 0 END), 0) as total_income, " +
            "COALESCE(SUM(CASE WHEN t.type = 0 THEN t.amount ELSE 0 END), 0) as total_expense " +
            "FROM biz_book b " +
            "LEFT JOIN biz_transaction t ON b.id = t.book_id AND t.deleted = 0 " +
            "WHERE b.user_id = #{userId} AND b.deleted = 0 " +
            "GROUP BY b.id, b.name ORDER BY b.id")
    List<BookBalanceDTO> sumBalanceByBook(@Param("userId") Long userId);

    /**
     * 账本余额汇总 DTO（用于 {@link #sumBalanceByBook} 返回值）。
     *
     * @param bookId       账本 ID
     * @param bookName     账本名称
     * @param totalIncome  总收入
     * @param totalExpense 总支出
     */
    record BookBalanceDTO(Long bookId, String bookName, BigDecimal totalIncome, BigDecimal totalExpense) {}

    /**
     * 按指定分类汇总支出金额（用于预算计算）。
     *
     * @param userId    用户 ID
     * @param categoryId 分类 ID
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @return 金额合计，无数据时为 null
     */
    /**
     * 按指定分类汇总支出金额（用于预算计算，支持含子分类）。
     *
     * @param userId      用户 ID
     * @param categoryIds 分类 ID 列表（含父分类+子分类）
     * @param startDate   起始日期
     * @param endDate     结束日期
     * @return 金额合计，无数据时为 null
     */
    @Select("<script>" +
            "SELECT SUM(amount) FROM biz_transaction " +
            "WHERE user_id = #{userId} AND type = 0 AND deleted = 0 " +
            "AND transaction_date BETWEEN #{startDate} AND #{endDate} " +
            "AND category_id IN " +
            "<foreach item='id' collection='categoryIds' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    BigDecimal sumExpenseByCategories(@Param("userId") Long userId,
                                      @Param("categoryIds") List<Long> categoryIds,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /**
     * 统计账本下的交易数量（用于删除前检查）。
     */
    @Select("SELECT COUNT(*) FROM biz_transaction WHERE book_id = #{bookId} AND deleted = 0")
    long countByBook(@Param("bookId") Long bookId);

    /**
     * 统计分类下的交易数量（用于删除前检查）。
     */
    @Select("SELECT COUNT(*) FROM biz_transaction WHERE category_id = #{categoryId} AND deleted = 0")
    long countByCategory(@Param("categoryId") Long categoryId);
}