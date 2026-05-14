package com.lex.wechatbill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lex.wechatbill.dto.CategoryCreateRequest;
import com.lex.wechatbill.entity.CategoryEntity;
import com.lex.wechatbill.mapper.CategoryMapper;
import com.lex.wechatbill.service.CategoryService;
import com.lex.wechatbill.vo.CategoryVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryVO> list() {
        return categoryMapper.selectList(null).stream()
            .map(this::toVO)
            .toList();
    }

    @Override
    public CategoryVO create(CategoryCreateRequest request) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(nextId());
        entity.setName(request.name());
        entity.setCategoryCode((request.categoryCode() != null && !request.categoryCode().isBlank() ? request.categoryCode() : request.name()).trim().toUpperCase().replace(' ', '_'));
        entity.setSource(request.source() == null || request.source().isBlank() ? \u0022manual\u0022 : request.source());
        entity.setEnabled(request.enabled() == null || request.enabled() ? 1 : 0);
        categoryMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public CategoryVO update(Integer id, CategoryCreateRequest request) {
        CategoryEntity entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException(\u0022Category not found\u0022);
        }
        entity.setName(request.name());
        entity.setCategoryCode((request.categoryCode() != null && !request.categoryCode().isBlank() ? request.categoryCode() : request.name()).trim().toUpperCase().replace(' ', '_'));
        entity.setSource(request.source() == null || request.source().isBlank() ? entity.getSource() : request.source());
        entity.setEnabled(request.enabled() == null ? entity.getEnabled() : (request.enabled() ? 1 : 0));
        categoryMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Integer id) {
        categoryMapper.deleteById(id);
    }

    private Integer nextId() {
        return categoryMapper.selectList(null).stream()
            .map(CategoryEntity::getId)
            .max(Integer::compareTo)
            .orElse(0) + 1;
    }

    private CategoryVO toVO(CategoryEntity entity) {
        return new CategoryVO(entity.getId(), entity.getCategoryCode(), entity.getName(), entity.getSource(), entity.getEnabled() == null || entity.getEnabled() != 0);
    }
}
