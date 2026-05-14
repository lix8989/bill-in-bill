package com.lex.wechatbill.service.impl;

import com.lex.wechatbill.entity.KeywordRuleEntity;
import com.lex.wechatbill.mapper.KeywordRuleMapper;
import com.lex.wechatbill.service.ClassifierService;
import com.lex.wechatbill.vo.ClassifyResultVO;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KeywordClassifierServiceImpl implements ClassifierService {

    private static final Logger log = LoggerFactory.getLogger(KeywordClassifierServiceImpl.class);
    private static final List<KeywordRule> FALLBACK_RULES = buildFallbackRules();

    private final KeywordRuleMapper keywordRuleMapper;
    private volatile List<KeywordRule> rules;

    public KeywordClassifierServiceImpl(KeywordRuleMapper keywordRuleMapper) {
        this.keywordRuleMapper = keywordRuleMapper;
        this.rules = new CopyOnWriteArrayList<>(FALLBACK_RULES);
    }

    @PostConstruct
    public void init() {
        refreshRules();
    }

    public void refreshRules() {
        try {
            List<KeywordRuleEntity> entities = keywordRuleMapper.selectList(null);
            if (entities == null || entities.isEmpty()) {
                log.info("No keyword rules in DB, using fallback rules");
                this.rules = new CopyOnWriteArrayList<>(FALLBACK_RULES);
                return;
            }
            Map<String, KeywordRule> map = new LinkedHashMap<>();
            for (KeywordRuleEntity e : entities) {
                if (e.getEnabled() == null || e.getEnabled() == 0) continue;
                map.computeIfAbsent(e.getCategoryCode(), k -> new KeywordRule(
                    e.getCategoryCode(), e.getConfidence(), e.getCategoryName(), new ArrayList<>()));
                map.get(e.getCategoryCode()).keywords().add(e.getKeyword());
            }
            this.rules = new CopyOnWriteArrayList<>(map.values());
            log.info("Loaded {} keyword rules from DB", rules.size());
        } catch (Exception ex) {
            log.warn("Failed to load keyword rules from DB, using fallback: {}", ex.getMessage());
            this.rules = new CopyOnWriteArrayList<>(FALLBACK_RULES);
        }
    }

    private static List<KeywordRule> buildFallbackRules() {
        List<KeywordRule> rules = new ArrayList<>();
        rules.add(new KeywordRule("DINING", 0.90, "餐饮",
            List.of("咖啡", "餐饮", "外卖", "食堂", "餐厅", "美食", "吃饭", "午餐", "晚餐", "早餐",
                "瑞幸", "肯德基", "麦当劳", "星巴克", "海底捞", "美团外卖", "饿了么")));
        rules.add(new KeywordRule("GROCERY", 0.85, "买菜生鲜",
            List.of("生鲜", "买菜", "蔬菜", "水果", "超市", "便利店", "永辉", "盒马", "沃尔玛",
                "大润发", "菜市场", "京东到家")));
        rules.add(new KeywordRule("SHOPPING", 0.85, "日用购物",
            List.of("购物", "网购", "淘宝", "京东", "拼多多", "唯品会", "天猫", "商城", "百货",
                "数码", "电器", "服装", "电商")));
        rules.add(new KeywordRule("TRANSPORT", 0.88, "交通出行",
            List.of("滴滴", "出租车", "地铁", "公交", "交通", "出行", "打车", "高铁", "火车",
                "飞机", "机票", "航旅", "高德", "百度地图", "网约车")));
        rules.add(new KeywordRule("LIVING_BILL", 0.85, "水电燃气",
            List.of("水电", "电费", "水费", "燃气", "煤气", "物业", "供暖")));
        rules.add(new KeywordRule("TELECOM", 0.88, "通讯网络",
            List.of("话费", "流量", "宽带", "中国移动", "中国联通", "中国电信", "通讯")));
        rules.add(new KeywordRule("MEDICAL", 0.88, "医疗健康",
            List.of("医院", "药店", "医药", "药品", "挂号", "体检", "医疗", "健康",
                "医保", "诊所")));
        rules.add(new KeywordRule("ENTERTAINMENT", 0.82, "娱乐休闲",
            List.of("电影", "游戏", "娱乐", "视频", "音乐", "会员", "充值", "直播",
                "腾讯视频", "爱奇艺", "网易云", "B站", "抖音")));
        rules.add(new KeywordRule("SOCIAL", 0.80, "人情往来",
            List.of("红包", "转账", "礼金", "随礼", "份子")));
        rules.add(new KeywordRule("FINANCE", 0.85, "金融理财",
            List.of("理财", "基金", "股票", "保险", "投资", "还款", "信用卡")));
        rules.add(new KeywordRule("RENT", 0.90, "租房",
            List.of("房租", "租金", "租房", "押金")));
        rules.add(new KeywordRule("CAR_FUEL", 0.92, "汽车加油",
            List.of("加油", "加油站", "中石化", "中石油", "壳牌")));
        rules.add(new KeywordRule("CAR_CHARGING", 0.90, "汽车充电",
            List.of("充电", "充电桩", "特来电", "星星充电")));
        rules.add(new KeywordRule("CAR_MAINTENANCE", 0.85, "停车/保养/车险",
            List.of("停车", "保养", "修车", "洗车", "车险", "年检", "违章", "过路费")));
        rules.add(new KeywordRule("TRANSFER", 0.85, "转账收付款",
            List.of("转账", "收款", "收付款")));
        rules.add(new KeywordRule("SALARY", 0.92, "工资收入",
            List.of("工资", "奖金", "补贴", "薪资", "薪酬")));
        rules.add(new KeywordRule("REFUND", 0.90, "退款退回",
            List.of("退款", "退货", "返现", "退还")));
        return rules;
    }

    @Override
    public ClassifyResultVO classify(String counterparty, String productName, String tradeType, Double amount) {
        String text = join(counterparty, productName, tradeType);
        if (text == null || text.isBlank()) {
            return new ClassifyResultVO(null, null, "OTHER", 0.50, "无有效分类信息");
        }
        text = text.toLowerCase();
        List<KeywordRule> currentRules = this.rules;
        for (KeywordRule rule : currentRules) {
            for (String keyword : rule.keywords) {
                if (text.contains(keyword.toLowerCase())) {
                    return new ClassifyResultVO(null, null, rule.categoryCode, rule.confidence,
                        "匹配关键字[" + keyword + "]，归类为" + rule.categoryName);
                }
            }
        }
        return new ClassifyResultVO(null, null, "OTHER", 0.55, "未匹配到明确分类规则");
    }

    @Override
    public List<ClassifyResultVO> classifyBatch(List<ClassifyInput> inputs) {
        List<ClassifyResultVO> results = new ArrayList<>();
        for (ClassifyInput input : inputs) {
            ClassifyResultVO result = classify(input.counterparty(), input.productName(), input.tradeType(), input.amount());
            results.add(new ClassifyResultVO(input.billId(), input.importKey(), result.categoryCode(), result.confidence(), result.reason()));
        }
        return results;
    }

    private String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                sb.append(part).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private record KeywordRule(String categoryCode, double confidence, String categoryName, List<String> keywords) {}
}
