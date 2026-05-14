package com.lex.wechatbill.controller;

import com.lex.wechatbill.common.ApiResponse;
import com.lex.wechatbill.dto.CategoryCreateRequest;
import com.lex.wechatbill.service.CategoryService;
import com.lex.wechatbill.vo.CategoryVO;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(\u0022/api/categories\u0022)
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryVO>> categories() {
        return ApiResponse.ok(categoryService.list());
    }

    @PostMapping
    public ApiResponse<CategoryVO> create(@RequestBody CategoryCreateRequest request) {
        return ApiResponse.ok(categoryService.create(request));
    }

    @PutMapping(\u0022/{id}\u0022)
    public ApiResponse<CategoryVO> update(@PathVariable Integer id, @RequestBody CategoryCreateRequest request) {
        return ApiResponse.ok(categoryService.update(id, request));
    }

    @DeleteMapping(\u0022/{id}\u0022)
    public ApiResponse<String> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ApiResponse.ok(\u0022OK\u0022);
    }
}
