package com.lex.wechatbill.service;

import com.lex.wechatbill.dto.ClassifyTaskRequest;
import com.lex.wechatbill.vo.ClassifyTaskVO;
import java.util.List;
import java.util.Map;

public interface ClassifyTaskService {

    ClassifyTaskVO createTask(ClassifyTaskRequest request);

    ClassifyTaskVO getTask(String taskNo);

    List<ClassifyTaskVO> listTasks();

    int previewCount(ClassifyTaskRequest request);

    Map<String, Integer> classifyStats();

    int resetAllCategories();
}
