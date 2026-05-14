package com.lex.wechatbill.vo;

/**
 * 年度对比数据
 */
public record YearlyComparisonData(
        LastYearData lastYear,         // 去年数据
        Double incomeGrowthRate,      // 收入增长率
        Double expenseGrowthRate,     // 支出增长率
        Double savingsRateChange      // 储蓄率变化
) {}
