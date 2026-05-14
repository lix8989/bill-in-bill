package com.lex.wechatbill.vo;

/**
 * 月度统计VO
 */
public record MonthlyStatVO(
        String month,                 // 月份（格式：2023-01）
        Double income,                // 月度收入
        Double expense,               // 月度支出
        Double balance,               // 月度结余
        Integer transactionCount,     // 月度交易次数
        Double avgTransactionAmount,  // 月度平均单笔金额
        String peakCategory           // 主要支出类别
) {}
