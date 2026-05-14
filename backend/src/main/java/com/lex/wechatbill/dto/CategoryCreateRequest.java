package com.lex.wechatbill.dto;

public record CategoryCreateRequest(String name, String categoryCode, String source, Boolean enabled) {
}
