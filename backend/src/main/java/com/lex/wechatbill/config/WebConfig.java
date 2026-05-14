package com.lex.wechatbill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(\u0022/api/**\u0022)
            .allowedOriginPatterns(\u0022http://localhost:*\u0022, \u0022http://127.0.0.1:*\u0022)
            .allowedMethods(\u0022GET\u0022, \u0022POST\u0022, \u0022PUT\u0022, \u0022DELETE\u0022, \u0022OPTIONS\u0022)
            .allowedHeaders(\u0022*\u0022);
    }
}
