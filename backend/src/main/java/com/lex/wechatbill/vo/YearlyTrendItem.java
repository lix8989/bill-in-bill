package com.lex.wechatbill.vo;

/**
 * 年度趋势项
 */
public record YearlyTrendItem(
        Integer year,                 // 年份
        Integer income,               // 年度收入
        Integer expense,              // 年度支出
        Integer balance,              // 年度结余
        Double savingsRate           // 储蓄率
) {}
