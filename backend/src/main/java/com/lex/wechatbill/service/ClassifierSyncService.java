package com.lex.wechatbill.service;

import com.lex.wechatbill.dto.CategorySyncBatchRequest;
import com.lex.wechatbill.vo.CategorySyncResultVO;

public interface ClassifierSyncService {

    CategorySyncResultVO syncCategories(CategorySyncBatchRequest request);
}
