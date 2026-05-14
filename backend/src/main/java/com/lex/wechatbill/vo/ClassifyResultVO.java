package com.lex.wechatbill.vo;

public record ClassifyResultVO(
    Integer billId,
    String importKey,
    String categoryCode,
    Double confidence,
    String reason
) {}
