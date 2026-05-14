package com.lex.wechatbill.dto;

import java.util.List;

public record CategorySyncBatchRequest(
    String syncBatchNo,
    List<CategorySyncItemRequest> items
) {
}
