package com.lex.wechatbill.vo;

public record CategorySyncResultVO(
    String syncBatchNo,
    int totalCount,
    int successCount,
    int failCount,
    int notFoundCount,
    int categoryMissingCount
) {
}
