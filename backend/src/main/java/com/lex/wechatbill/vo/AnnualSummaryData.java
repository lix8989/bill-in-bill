package com.lex.wechatbill.vo;

/**
 * 年度基础财务数据
 */
public record AnnualSummaryData(
        Integer totalIncome,           // 年度总收入
        Integer totalExpense,          // 年度总支出
        Integer balance,               // 年度结余
        Integer transactionCount,      // 交易总次数
        Double avgMonthlyIncome,       // 月均收入
        Double avgMonthlyExpense,      // 月均支出
        Double savingsRate            // 储蓄率（结余/收入）
) {}
