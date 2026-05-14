package com.lex.wechatbill.vo;

import java.util.List;

/**
 * 年度洞察数据
 */
public record AnnualInsightsData(
        List<String> keywords,       // 年度关键词
        String yearSummary,          // 年度总结
        List<String> suggestions,    // 改进建议
        String encouragement          // 鼓励话语
) {}
