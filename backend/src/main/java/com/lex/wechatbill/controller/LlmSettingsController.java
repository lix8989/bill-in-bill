package com.lex.wechatbill.controller;

import com.lex.wechatbill.common.ApiResponse;
import com.lex.wechatbill.service.LlmClientService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classifier/llm-settings")
public class LlmSettingsController {

    private final LlmClientService llmClientService;

    public LlmSettingsController(LlmClientService llmClientService) {
        this.llmClientService = llmClientService;
    }

    @GetMapping
    public ApiResponse<Map<String, String>> getSettings() {
        return ApiResponse.ok(llmClientService.getAllSettings());
    }

    @PutMapping
    public ApiResponse<String> updateSettings(@RequestBody Map<String, String> settings) {
        llmClientService.updateSettings(settings);
        return ApiResponse.ok("OK");
    }
}
