package com.lex.wechatbill.service;

import com.lex.wechatbill.dto.CategoryCreateRequest;
import com.lex.wechatbill.vo.CategoryVO;
import java.util.List;

public interface CategoryService {

    List<CategoryVO> list();

    CategoryVO create(CategoryCreateRequest request);

    CategoryVO update(Integer id, CategoryCreateRequest request);

    void delete(Integer id);
}
