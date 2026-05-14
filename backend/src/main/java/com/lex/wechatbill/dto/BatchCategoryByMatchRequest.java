package com.lex.wechatbill.dto;

public record BatchCategoryByMatchRequest(String counterparty, String productName, Integer categoryId, String categorySyncStatus) {
}
