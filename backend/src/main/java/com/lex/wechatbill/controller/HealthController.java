package com.lex.wechatbill.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(\u0022/api/health\u0022)
public class HealthController {

    @GetMapping
    public Map<String, String> health() {
        return Map.of(\u0022status\u0022, \u0022UP\u0022);
    }
}
