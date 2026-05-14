package com.lex.wechatbill.dto;

public record CategorySyncItemRequest(
    Integer billId,
    String importKey,
    String categoryCode,
    Double confidence,
    String reason
) {
}
