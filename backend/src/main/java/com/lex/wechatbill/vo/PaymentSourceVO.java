package com.lex.wechatbill.vo;

/**
 * 支付来源VO（微信/支付宝）
 */
public record PaymentSourceVO(
        String sourceName,            // 来源名称（微信/支付宝）
        Double amount,                 // 消费总额
        Integer transactionCount,      // 交易笔数
        Double percentage             // 占比（0-1）
) {}
