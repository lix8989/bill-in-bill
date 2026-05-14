package com.lex.wechatbill.vo;

import java.util.List;

/**
 * 多年度趋势数据
 */
public record MultiYearTrendData(
        List<YearlyTrendItem> trends,  // 年度趋势列表
        String growthSummary,         // 增长总结
        String forecast               // 展望描述
) {}
