package com.lex.wechatbill.vo;

import java.util.List;

public record ImportResultVO(
    Integer totalCount,
    Integer successCount,
    Integer failCount,
    List<ImportFailDetailVO> failDetails
) {
}
