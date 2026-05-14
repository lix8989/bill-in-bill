package com.lex.wechatbill.vo;

import java.util.List;

public record DashboardVO(
    Integer selectedYear,
    String selectedMonth,
    Integer yearlyIncome,
    Integer yearlyExpense,
    Integer monthlyIncome,
    Integer monthlyExpense,
    List<DashboardCategoryVO> yearlyCategoryStats,
    List<DashboardCategoryVO> monthlyCategoryStats,
    List<DashboardTopItemVO> yearlyTopItems,
    List<DashboardTopItemVO> monthlyTopItems,
    List<DashboardMonthVO> yearMonthTrend,
    DashboardSourceVO yearlyIncomeBySource,
    DashboardSourceVO yearlyExpenseBySource,
    DashboardSourceVO monthlyIncomeBySource,
    DashboardSourceVO monthlyExpenseBySource,
    List<DashboardMonthVO> yearMonthTrendWechat,
    List<DashboardMonthVO> yearMonthTrendAlipay,
    List<DashboardCategoryVO> yearlyCategoryStatsWechat,
    List<DashboardCategoryVO> yearlyCategoryStatsAlipay,
    List<DashboardDayVO> monthlyDayTrend
) {
}
