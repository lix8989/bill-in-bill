package com.lex.wechatbill.vo;

/**
 * 收入来源VO
 */
public record IncomeSourceVO(
        String sourceName,            // 来源名称（工资、理财、其他）
        Double amount,                // 金额
        Double percentage,            // 占比（0-1）
        Integer transactionCount,     // 笔数
        String trend                  // 趋势描述
) {}
