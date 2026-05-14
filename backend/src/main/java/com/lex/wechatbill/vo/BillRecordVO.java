package com.lex.wechatbill.vo;

public record BillRecordVO(
    Integer id,
    String tradeTime,
    String tradeType,
    String incomeExpenseType,
    String counterparty,
    String productName,
    Double amount,
    String payMethod,
    String tradeStatus,
    String tradeNo,
    String merchantOrderNo,
    String remark,
    Integer categoryId,
    String categoryName,
    Boolean settlementIncluded,
    String sourceFileName,
    String categorySyncStatus,
    Double categoryConfidence,
    String categorySource,
    String categorySyncReason,
    String categorySyncAt,
    String source
) {
}
