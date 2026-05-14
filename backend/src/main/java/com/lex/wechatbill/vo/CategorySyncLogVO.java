package com.lex.wechatbill.vo;

public record CategorySyncLogVO(
    Integer id,
    String syncBatchNo,
    Integer billId,
    String importKey,
    String requestedCategoryCode,
    Integer resolvedCategoryId,
    Double confidence,
    String reason,
    String status,
    String message
) {
}
