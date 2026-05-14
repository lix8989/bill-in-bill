package com.lex.wechatbill.controller;

import com.lex.wechatbill.common.ApiResponse;
import com.lex.wechatbill.dto.ClassifyTaskRequest;
import com.lex.wechatbill.service.ClassifyTaskService;
import com.lex.wechatbill.vo.ClassifyTaskVO;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classifier/tasks")
public class ClassifyTaskController {

    private final ClassifyTaskService classifyTaskService;

    public ClassifyTaskController(ClassifyTaskService classifyTaskService) {
        this.classifyTaskService = classifyTaskService;
    }

    @PostMapping("/auto-classify")
    public ApiResponse<ClassifyTaskVO> autoClassify(@RequestBody ClassifyTaskRequest request) {
        return ApiResponse.ok(classifyTaskService.createTask(request));
    }

    @GetMapping("/preview")
    public ApiResponse<Map<String, Object>> preview(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String month,
        @RequestParam(required = false, defaultValue = "false") boolean reclassify) {
        ClassifyTaskRequest request = new ClassifyTaskRequest(null, year, month, null, null, reclassify);
        int count = classifyTaskService.previewCount(request);
        return ApiResponse.ok(Map.of("count", count, "reclassify", reclassify));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Integer>> stats() {
        return ApiResponse.ok(classifyTaskService.classifyStats());
    }

    @PostMapping("/reset")
    public ApiResponse<Map<String, Integer>> reset() {
        int count = classifyTaskService.resetAllCategories();
        return ApiResponse.ok(Map.of("resetCount", count));
    }

    @GetMapping
    public ApiResponse<List<ClassifyTaskVO>> tasks() {
        return ApiResponse.ok(classifyTaskService.listTasks());
    }

    @GetMapping("/{taskNo}")
    public ApiResponse<ClassifyTaskVO> task(@PathVariable String taskNo) {
        return ApiResponse.ok(classifyTaskService.getTask(taskNo));
    }
}
