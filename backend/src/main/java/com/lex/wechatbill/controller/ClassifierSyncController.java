package com.lex.wechatbill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lex.wechatbill.common.ApiResponse;
import com.lex.wechatbill.dto.CategorySyncBatchRequest;
import com.lex.wechatbill.entity.CategorySyncLogEntity;
import com.lex.wechatbill.mapper.CategorySyncLogMapper;
import com.lex.wechatbill.service.CategoryService;
import com.lex.wechatbill.service.ClassifierSyncService;
import com.lex.wechatbill.vo.CategorySyncLogVO;
import com.lex.wechatbill.vo.CategorySyncResultVO;
import com.lex.wechatbill.vo.CategoryVO;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classifier/sync")
public class ClassifierSyncController {

    private final ClassifierSyncService classifierSyncService;
    private final CategorySyncLogMapper categorySyncLogMapper;
    private final CategoryService categoryService;

    public ClassifierSyncController(ClassifierSyncService classifierSyncService, CategorySyncLogMapper categorySyncLogMapper, CategoryService categoryService) {
        this.classifierSyncService = classifierSyncService;
        this.categorySyncLogMapper = categorySyncLogMapper;
        this.categoryService = categoryService;
    }

    @GetMapping("/logs")
    public ApiResponse<Map<String, Object>> logs(
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int offset = (safePage - 1) * safePageSize;

        long total = categorySyncLogMapper.selectCount(null);
        List<CategorySyncLogVO> records = categorySyncLogMapper.selectList(
            new QueryWrapper<CategorySyncLogEntity>().orderByDesc("id").last("limit " + safePageSize + " offset " + offset)
        ).stream()
            .map(item -> new CategorySyncLogVO(item.getId(), item.getSyncBatchNo(), item.getBillId(), item.getImportKey(), item.getRequestedCategoryCode(), item.getResolvedCategoryId(), item.getConfidence(), item.getReason(), item.getStatus(), item.getMessage()))
            .toList();

        return ApiResponse.ok(Map.of("total", total, "records", records, "page", safePage, "pageSize", safePageSize));
    }

    @GetMapping("/categories/mappings")
    public ApiResponse<List<CategoryVO>> mappings() {
        return ApiResponse.ok(categoryService.list());
    }

    @PostMapping("/categories")
    public ApiResponse<CategorySyncResultVO> syncCategories(@RequestBody CategorySyncBatchRequest request) {
        return ApiResponse.ok(classifierSyncService.syncCategories(request));
    }
}
