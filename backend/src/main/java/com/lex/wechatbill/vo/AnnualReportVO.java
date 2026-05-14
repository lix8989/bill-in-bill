package com.lex.wechatbill.vo;

import java.util.List;

/**
 * 年度账本VO
 * 包含年度报告的完整数据
 */
public record AnnualReportVO(
        // 年份
        Integer year,

        // === 基础财务数据 ===
        AnnualSummaryData summary,

        // === 年度对比数据 ===
        YearlyComparisonData yearlyComparison,

        // === 月度统计数据 ===
        List<MonthlyStatVO> monthlyStats,

        // === 分类统计数据 ===
        List<CategoryStatVO> categoryStats,

        // === 收入来源数据 ===
        List<IncomeSourceVO> incomeSources,

        // === 消费来源数据（微信/支付宝） ===
        List<PaymentSourceVO> paymentSources,

        // === 消费习惯数据 ===
        SpendingHabitsData spendingHabits,

        // === 多年度趋势数据 ===
        MultiYearTrendData multiYearTrend,

        // === 智能洞察数据 ===
        AnnualInsightsData insights
) {
}
