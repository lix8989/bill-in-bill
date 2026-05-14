package com.lex.wechatbill.dto;

public record BillCreateRequest(
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
    Boolean settlementIncluded
) {
}
