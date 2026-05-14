package com.lex.wechatbill.service;

import com.lex.wechatbill.entity.LlmSettingsEntity;
import com.lex.wechatbill.mapper.LlmSettingsMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LlmClientService {

    private final LlmSettingsMapper llmSettingsMapper;
    private final ObjectMapper objectMapper;

    public LlmClientService(LlmSettingsMapper llmSettingsMapper, ObjectMapper objectMapper) {
        this.llmSettingsMapper = llmSettingsMapper;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        String key = getSetting("api_key");
        return key != null && !key.isBlank();
    }

    public String classify(String systemPrompt, String userMessage) {
        String apiUrl = getSetting("api_url", "https://api.openai.com/v1/chat/completions");
        String apiKey = getSetting("api_key", "");
        String model = getSetting("model_name", "gpt-4o-mini");
        int maxTokens = Integer.parseInt(getSetting("max_tokens", "1024"));
        double temperature = Double.parseDouble(getSetting("temperature", "0.1"));

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", maxTokens);
            body.put("temperature", temperature);

            ArrayNode messages = body.putArray("messages");
            ObjectNode sysMsg = messages.addObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            String requestBody = objectMapper.writeValueAsString(body);

            java.net.URI uri = new java.net.URI(apiUrl);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(java.time.Duration.ofSeconds(120))
                .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("LLM API error: " + response.statusCode() + " - " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choice = root.path("choices").get(0);
            if (choice == null) {
                throw new RuntimeException("LLM response missing choices");
            }
            return choice.path("message").path("content").asText("").trim();
        } catch (Exception e) {
            throw new RuntimeException("LLM call failed: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getAllSettings() {
        return llmSettingsMapper.selectList(null).stream()
            .collect(Collectors.toMap(LlmSettingsEntity::getConfigKey, LlmSettingsEntity::getConfigValue));
    }

    public void updateSettings(Map<String, String> settings) {
        settings.forEach((key, value) -> {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LlmSettingsEntity> query =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LlmSettingsEntity>()
                    .eq("config_key", key);
            LlmSettingsEntity existing = llmSettingsMapper.selectOne(query.last("limit 1"));
            if (existing != null) {
                existing.setConfigValue(value);
                llmSettingsMapper.updateById(existing);
            } else {
                LlmSettingsEntity entity = new LlmSettingsEntity();
                entity.setConfigKey(key);
                entity.setConfigValue(value);
                llmSettingsMapper.insert(entity);
            }
        });
    }

    private String getSetting(String key) {
        return getSetting(key, null);
    }

    private String getSetting(String key, String defaultValue) {
        LlmSettingsEntity entity = llmSettingsMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LlmSettingsEntity>()
                .eq("config_key", key).last("limit 1"));
        return entity == null ? defaultValue : entity.getConfigValue();
    }
}
