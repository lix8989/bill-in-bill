package com.lex.wechatbill.vo;

public record ImportHistoryVO(
    Integer id,
    String sourceFileName,
    Integer totalCount,
    Integer successCount,
    Integer failCount,
    String message,
    String createdAt,
    String source
) {
}
