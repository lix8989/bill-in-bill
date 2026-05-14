package com.lex.wechatbill.vo;

import java.util.List;

/**
 * 分类统计VO
 */
public record CategoryStatVO(
        String categoryName,          // 分类名称
        Double amount,                // 分类金额
        Double percentage,            // 占比（0-1）
        Integer transactionCount,     // 交易次数
        Double avgAmount,             // 平均金额
        List<String> topProducts      // Top商品列表
) {}
