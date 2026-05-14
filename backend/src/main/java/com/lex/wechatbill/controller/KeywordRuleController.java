package com.lex.wechatbill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lex.wechatbill.common.ApiResponse;
import com.lex.wechatbill.entity.CategoryEntity;
import com.lex.wechatbill.entity.KeywordRuleEntity;
import com.lex.wechatbill.mapper.CategoryMapper;
import com.lex.wechatbill.mapper.KeywordRuleMapper;
import com.lex.wechatbill.service.impl.KeywordClassifierServiceImpl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classifier/keyword-rules")
public class KeywordRuleController {

    private final KeywordRuleMapper keywordRuleMapper;
    private final CategoryMapper categoryMapper;
    private final KeywordClassifierServiceImpl keywordClassifier;

    public KeywordRuleController(KeywordRuleMapper keywordRuleMapper, CategoryMapper categoryMapper, KeywordClassifierServiceImpl keywordClassifier) {
        this.keywordRuleMapper = keywordRuleMapper;
        this.categoryMapper = categoryMapper;
        this.keywordClassifier = keywordClassifier;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listRules() {
        List<KeywordRuleEntity> all = keywordRuleMapper.selectList(null);
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (KeywordRuleEntity rule : all) {
            if (rule.getEnabled() == null || rule.getEnabled() == 0) continue;
            grouped.computeIfAbsent(rule.getCategoryCode(), k -> {
                Map<String, Object> g = new LinkedHashMap<>();
                g.put("categoryCode", rule.getCategoryCode());
                g.put("categoryName", rule.getCategoryName());
                g.put("confidence", rule.getConfidence());
                g.put("keywords", new ArrayList<String>());
                return g;
            });
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) grouped.get(rule.getCategoryCode()).get("keywords");
            if (!keywords.contains(rule.getKeyword())) {
                keywords.add(rule.getKeyword());
            }
        }

        List<CategoryEntity> allCategories = categoryMapper.selectList(
            new QueryWrapper<CategoryEntity>().eq("enabled", 1));
        for (CategoryEntity cat : allCategories) {
            String code = cat.getCategoryCode();
            if (code == null || code.isBlank()) continue;
            if (!grouped.containsKey(code)) {
                Map<String, Object> g = new LinkedHashMap<>();
                g.put("categoryCode", code);
                g.put("categoryName", cat.getName() != null ? cat.getName() : code);
                g.put("confidence", 0.85);
                g.put("keywords", new ArrayList<String>());
                grouped.put(code, g);
            }
        }

        return ApiResponse.ok(new ArrayList<>(grouped.values()));
    }

    @PostMapping("/save-category")
    public ApiResponse<String> saveCategory(@RequestBody Map<String, Object> body) {
        String categoryCode = ((String) body.get("categoryCode")).toUpperCase();
        String categoryName = (String) body.get("categoryName");
        double confidence = body.get("confidence") instanceof Number
            ? ((Number) body.get("confidence")).doubleValue() : 0.85;
        @SuppressWarnings("unchecked")
        List<String> keywords = (List<String>) body.get("keywords");

        ensureCategoryExists(categoryCode, categoryName);

        keywordRuleMapper.delete(
            new QueryWrapper<KeywordRuleEntity>().eq("category_code", categoryCode));

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (keywords != null) {
            for (int i = 0; i < keywords.size(); i++) {
                KeywordRuleEntity entity = new KeywordRuleEntity();
                entity.setCategoryCode(categoryCode);
                entity.setCategoryName(categoryName);
                entity.setKeyword(keywords.get(i));
                entity.setConfidence(confidence);
                entity.setSortOrder(i);
                entity.setEnabled(1);
                entity.setCreatedAt(now);
                entity.setUpdatedAt(now);
                keywordRuleMapper.insert(entity);
            }
        }
        keywordClassifier.refreshRules();
        return ApiResponse.ok("OK");
    }

    private void ensureCategoryExists(String categoryCode, String categoryName) {
        CategoryEntity existing = categoryMapper.selectOne(
            new QueryWrapper<CategoryEntity>().eq("category_code", categoryCode).last("limit 1"));
        if (existing != null) return;
        CategoryEntity entity = new CategoryEntity();
        int maxId = categoryMapper.selectList(null).stream()
            .map(CategoryEntity::getId).max(Integer::compareTo).orElse(0);
        entity.setId(maxId + 1);
        entity.setName(categoryName);
        entity.setCategoryCode(categoryCode);
        entity.setSource("system");
        entity.setEnabled(1);
        entity.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        categoryMapper.insert(entity);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRule(@PathVariable Integer id) {
        keywordRuleMapper.deleteById(id);
        keywordClassifier.refreshRules();
        return ApiResponse.ok("OK");
    }

    @PostMapping("/delete-category")
    public ApiResponse<String> deleteCategory(@RequestBody Map<String, String> body) {
        String categoryCode = body.get("categoryCode");
        if (categoryCode == null || categoryCode.isBlank()) {
            return ApiResponse.fail("categoryCode is required");
        }
        keywordRuleMapper.delete(
            new QueryWrapper<KeywordRuleEntity>().eq("category_code", categoryCode.toUpperCase()));
        keywordClassifier.refreshRules();
        return ApiResponse.ok("OK");
    }

    @PostMapping("/refresh")
    public ApiResponse<String> refresh() {
        keywordClassifier.refreshRules();
        return ApiResponse.ok("OK");
    }
}
