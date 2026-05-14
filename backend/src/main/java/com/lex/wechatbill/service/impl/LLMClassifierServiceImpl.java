package com.lex.wechatbill.service.impl;

import com.lex.wechatbill.entity.BillRecordEntity;
import com.lex.wechatbill.entity.CategoryEntity;
import com.lex.wechatbill.mapper.BillRecordMapper;
import com.lex.wechatbill.mapper.CategoryMapper;
import com.lex.wechatbill.service.ClassifierService;
import com.lex.wechatbill.service.LlmClientService;
import com.lex.wechatbill.vo.ClassifyResultVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class LLMClassifierServiceImpl implements ClassifierService {

    private static final Logger log = LoggerFactory.getLogger(LLMClassifierServiceImpl.class);
    private static final int MAX_FEW_SHOT = 6;

    private final LlmClientService llmClient;
    private final CategoryMapper categoryMapper;
    private final BillRecordMapper billRecordMapper;
    private final ObjectMapper objectMapper;
    private final KeywordClassifierServiceImpl fallback;

    public LLMClassifierServiceImpl(LlmClientService llmClient, CategoryMapper categoryMapper,
                                     BillRecordMapper billRecordMapper, ObjectMapper objectMapper,
                                     KeywordClassifierServiceImpl fallback) {
        this.llmClient = llmClient;
        this.categoryMapper = categoryMapper;
        this.billRecordMapper = billRecordMapper;
        this.objectMapper = objectMapper;
        this.fallback = fallback;
    }

    @Override
    public ClassifyResultVO classify(String counterparty, String productName, String tradeType, Double amount) {
        List<ClassifyInput> inputs = List.of(new ClassifyInput(null, null, counterparty, productName, tradeType, amount));
        return classifyBatch(inputs).getFirst();
    }

    @Override
    public List<ClassifyResultVO> classifyBatch(List<ClassifyInput> inputs) {
        if (!llmClient.isConfigured()) {
            log.info("LLM not configured, using keyword fallback");
            return fallback.classifyBatch(inputs);
        }

        String systemPrompt = buildSystemPrompt();
        List<ClassifyResultVO> results = new ArrayList<>();

        for (ClassifyInput input : inputs) {
            String userMessage = buildUserMessage(input);
            String fewShot = buildFewShot();
            String fullPrompt = fewShot + "\n\n" + userMessage;
            try {
                String response = llmClient.classify(systemPrompt, fullPrompt);
                ClassifyResultVO parsed = parseResponse(response, input.billId(), input.importKey());
                results.add(parsed);
            } catch (Exception e) {
                log.warn("LLM classify failed for bill {}, fallback to keyword: {}", input.billId(), e.getMessage());
                ClassifyResultVO fallbackResult = fallback.classify(input.counterparty(), input.productName(), input.tradeType(), input.amount());
                results.add(new ClassifyResultVO(input.billId(), input.importKey(),
                    fallbackResult.categoryCode(), fallbackResult.confidence(),
                    fallbackResult.reason() + " (LLM fallback)"));
            }
        }
        return results;
    }

    private String buildSystemPrompt() {
        List<CategoryEntity> categories = categoryMapper.selectList(null);
        String categoryList = categories.stream()
            .filter(c -> c.getEnabled() == null || c.getEnabled() == 1)
            .map(c -> "- " + c.getCategoryCode() + ": " + c.getName())
            .collect(Collectors.joining("\n"));

        return """
            你是一个账单分类助手，请根据账单信息从给定的分类列表中选择最合适的分类。

            分类列表：
            %s

            要求：
            1. 只能从上面列表中选择一个分类，返回对应的分类编码。
            2. 如果无法准确判断，返回 OTHER。
            3. 输出必须是严格的 JSON 格式，不要输出额外说明。

            返回格式：
            {
              "categoryCode": "分类编码",
              "confidence": 0.0 到 1.0 之间的数值,
              "reason": "分类原因说明"
            }
            """.formatted(categoryList);
    }

    private String buildFewShot() {
        List<BillRecordEntity> samples = billRecordMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BillRecordEntity>()
                .isNotNull("category_id")
                .eq("category_source", "manual")
                .orderByDesc("id")
                .last("limit " + MAX_FEW_SHOT));

        if (samples.isEmpty()) {
            samples = billRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BillRecordEntity>()
                    .isNotNull("category_id")
                    .eq("category_sync_status", "success")
                    .orderByDesc("id")
                    .last("limit " + MAX_FEW_SHOT));
        }

        if (samples.isEmpty()) {
            samples = billRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BillRecordEntity>()
                    .isNotNull("category_id")
                    .orderByDesc("id")
                    .last("limit " + MAX_FEW_SHOT));
        }

        if (samples.isEmpty()) {
            return "";
        }

        java.util.Map<Integer, String> idToCode = categoryMapper.selectList(null).stream()
            .collect(Collectors.toMap(c -> c.getId(), c -> c.getCategoryCode()));

        StringBuilder sb = new StringBuilder("以下是人工正确分类的参考样本：\n");
        for (int i = 0; i < samples.size(); i++) {
            BillRecordEntity bill = samples.get(i);
            String code = idToCode.getOrDefault(bill.getCategoryId(), "OTHER");
            sb.append("\n示例").append(i + 1).append(":\n");
            sb.append("交易对方：").append(nullToEmpty(bill.getCounterparty())).append("\n");
            sb.append("商品说明：").append(nullToEmpty(bill.getProductName())).append("\n");
            sb.append("交易类型：").append(nullToEmpty(bill.getTradeType())).append("\n");
            sb.append("金额：").append(bill.getAmount() == null ? "0" : String.valueOf(bill.getAmount())).append("\n");
            sb.append("分类：").append(code).append("\n");
        }
        return sb.toString();
    }

    private String buildUserMessage(ClassifyInput input) {
        return "请分类以下账单：\n"
            + "- 交易对方：" + nullToEmpty(input.counterparty()) + "\n"
            + "- 商品说明：" + nullToEmpty(input.productName()) + "\n"
            + "- 交易类型：" + nullToEmpty(input.tradeType()) + "\n"
            + "- 金额：" + (input.amount() == null ? "0" : String.valueOf(input.amount()));
    }

    private ClassifyResultVO parseResponse(String response, Integer billId, String importKey) {
        String json = response.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("(?s)```(?:json)?\\s*", "").trim();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            String code = node.path("categoryCode").asText("OTHER").trim().toUpperCase();
            double confidence = node.path("confidence").asDouble(0.5);
            String reason = node.path("reason").asText("");
            if (code.isBlank()) code = "OTHER";
            return new ClassifyResultVO(billId, importKey, code, confidence, reason);
        } catch (Exception e) {
            log.warn("Failed to parse LLM response: {}, raw: {}", e.getMessage(), response);
            return new ClassifyResultVO(billId, importKey, "OTHER", 0.4, "解析模型返回结果失败");
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
