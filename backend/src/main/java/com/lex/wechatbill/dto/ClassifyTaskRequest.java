package com.lex.wechatbill.dto;

public record ClassifyTaskRequest(
    String taskType,
    Integer year,
    String month,
    Integer categoryId,
    String incomeExpenseType,
    Boolean reclassify
) {}
