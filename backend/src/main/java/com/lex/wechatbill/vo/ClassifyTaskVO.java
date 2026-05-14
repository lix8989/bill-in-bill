package com.lex.wechatbill.vo;

public record ClassifyTaskVO(
    Integer id,
    String taskNo,
    String taskType,
    Integer totalCount,
    Integer successCount,
    Integer failCount,
    String status,
    String errorMessage,
    String startedAt,
    String finishedAt,
    String createdAt
) {}
