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
}