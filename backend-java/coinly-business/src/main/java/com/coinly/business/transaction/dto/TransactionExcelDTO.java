package com.coinly.business.transaction.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * V9.1: 交易记录 Excel 导出 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionExcelDTO {

    @ExcelProperty("交易日期")
    private String transactionDate;

    @ExcelProperty("类型")
    private String type;

    @ExcelProperty("分类")
    private String categoryName;

    @ExcelProperty("金额")
    private BigDecimal amount;

    @ExcelProperty("备注")
    private String remark;
}
