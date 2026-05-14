package com.lex.wechatbill.service;

import com.lex.wechatbill.vo.AnnualInsightsData;
import com.lex.wechatbill.vo.AnnualSummaryData;
import com.lex.wechatbill.vo.SpendingHabitsData;
import com.lex.wechatbill.vo.YearlyComparisonData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 年度账本智能文案生成服务
 * 负责根据数据生成有温度的个性化描述
 */
@Service
public class AnnualReportCopywriterService {

    /**
     * 生成年度洞察数据
     */
    public AnnualInsightsData generateInsights(
            Integer year,
            AnnualSummaryData summary,
            YearlyComparisonData yearlyComparison,
            SpendingHabitsData spendingHabits
    ) {
        return new AnnualInsightsData(
                generateKeywords(spendingHabits),
                generateYearSummary(year, summary, yearlyComparison, spendingHabits),
                generateSuggestions(year, summary, yearlyComparison),
                generateEncouragement()
        );
    }

    /**
     * 生成年度关键词
     */
    private List<String> generateKeywords(SpendingHabitsData habits) {
        List<String> keywords = new ArrayList<>();

        if (habits != null && habits.tags() != null) {
            keywords.addAll(habits.tags());
        }

        // 根据用户画像添加默认关键词
        if (keywords.isEmpty()) {
            keywords.add("理性消费");
            keywords.add("稳健理财");
        }

        return keywords;
    }

    /**
     * 生成年度总结文案
     */
    private String generateYearSummary(
            Integer year,
            AnnualSummaryData summary,
            YearlyComparisonData comparison,
            SpendingHabitsData habits
    ) {
        StringBuilder sb = new StringBuilder();

        // 开场白
        sb.append(String.format("这一年，你一共支付了%d次，", summary.transactionCount()));
        sb.append(String.format("平均每天%.1f笔交易。\n",
            (double) summary.transactionCount() / 365));

        // 核心数据
        sb.append(String.format("你赚到了%s元，", formatAmount(summary.totalIncome())));
        sb.append(String.format("花掉了%s元，", formatAmount(summary.totalExpense())));
        sb.append(String.format("结余%s元。\n", formatAmount(summary.balance())));

        // 储蓄率评价
        sb.append("你的年度关键词是：");
        sb.append(String.format("%.0f%%的储蓄率", summary.savingsRate() * 100));

        if (comparison != null && comparison.incomeGrowthRate() != null) {
            if (comparison.incomeGrowthRate() > 0) {
                sb.append("、稳步成长");
            } else {
                sb.append("、理性消费");
            }
        }

        sb.append("。\n\n");

        // 个性化描述
        if (habits != null && habits.personaDescription() != null) {
            sb.append(habits.personaDescription());
        }

        return sb.toString();
    }

    /**
     * 生成改进建议
     */
    private List<String> generateSuggestions(
            Integer year,
            AnnualSummaryData summary,
            YearlyComparisonData comparison
    ) {
        List<String> suggestions = new ArrayList<>();

        // 储蓄建议
        if (summary.savingsRate() < 0.4) {
            suggestions.add(String.format(
                "建议将储蓄率提升到40%%以上，每年可多储蓄%s元。" +
                "考虑将部分收入用于定期存款或低风险理财产品。",
                formatAmount((int)(summary.totalIncome() * 0.1))
            ));
        } else if (summary.savingsRate() > 0.5) {
            suggestions.add("你的储蓄率已经很高了，可以考虑增加投资理财的比重，让钱为你赚钱。");
        } else {
            suggestions.add("建议适当增加储蓄，每月可以设置一个固定的储蓄目标，养成良好的理财习惯。");
        }

        // 收入来源建议
        suggestions.add("收入来源过于单一会增加财务风险，建议学习更多投资理财知识，" +
            "考虑指数基金、国债等低风险投资产品，逐步实现收入来源多元化。");

        // 消费习惯建议
        if (comparison != null && comparison.expenseGrowthRate() != null) {
            if (comparison.expenseGrowthRate() > 0.1) {
                suggestions.add("今年支出增长较快，建议设置月度消费上限，避免冲动消费。" +
                    "大额消费前先思考24小时，培养理性消费习惯。");
            } else if (comparison.expenseGrowthRate() < 0) {
                suggestions.add("今年你成功控制了支出，消费观念更加成熟，继续保持这种良好的理财习惯！");
            }
        }

        // 年度目标建议
        suggestions.add(String.format(
            "按照今年的财务表现，明年你的收入有望突破%s，" +
            "储蓄率也有望提升到%d%%以上。继续保持，财务自由的目标越来越近了！",
            formatAmount((int)(summary.totalIncome() * 1.15)),
            (int)(summary.savingsRate() * 100 + 5)
        ));

        return suggestions;
    }

    /**
     * 生成鼓励话语
     */
    private String generateEncouragement() {
        return "每一笔支出，都是生活的印记。\n" +
                "每一份收入，都值得被铭记。\n\n" +
                "明年这个时候，我们再相见。\n" +
                "期待看到更好的你！";
    }

    /**
     * 生成用户画像文案
     */
    public String generatePersonaCopy(
            String personaType,
            List<String> tags,
            Double avgTransactionAmount,
            Integer weekendAvg,
            Integer weekdayAvg
    ) {
        StringBuilder sb = new StringBuilder();

        // 根据标签生成描述
        if (tags.contains("餐饮达人")) {
            sb.append("你是个标准的\"吃货\"——餐饮美食是你最大的支出类别。\n");
            sb.append("这说明你很注重生活品质，喜欢用美食来犒劳自己。\n");
            sb.append("建议可以适当增加在家做饭的频率，既健康又能节省开支。\n\n");
        }

        if (tags.contains("夜猫子")) {
            sb.append("你是个典型的\"夜猫子\"消费者，晚上8点到11点是你最活跃的消费时段。\n");
            sb.append("这或许说明，你习惯在工作一天后，用美食和购物来犒劳自己。\n");
            sb.append("适度的奖励是必要的，但也要注意不要\"冲动消费\"呀。\n\n");
        }

        if (weekendAvg != null && weekdayAvg != null && weekendAvg > weekdayAvg * 1.5) {
            sb.append("你在周末的消费是工作日的%.1f倍，这说明你很懂得\"劳逸结合\"——\n");
            sb.append(String.format("工作日比较节俭，周末就会好好犒劳自己。周末消费：%.0f元，工作日消费：%.0f元。\n",
                (double) weekendAvg / weekdayAvg, (double) weekendAvg, (double) weekdayAvg));
            sb.append("这种消费模式其实很健康，既有工作的自律，也有生活的享受。\n\n");
        }

        return sb.toString();
    }

    /**
     * 生成月度洞察文案
     */
    public String generateMonthlyInsight(
            String month,
            Double monthlyExpense,
            Double avgMonthlyExpense,
            String peakCategory
    ) {
        double ratio = avgMonthlyExpense > 0 ? monthlyExpense / avgMonthlyExpense : 1;

        if (ratio > 1.3) {
            return String.format(
                "%s是你最\"放纵\"的月份，单月支出了%s元，比月均支出高出%.0f%%。\n" +
                "这或许是因为那场期待已久的旅行，也可能是给家人添置了心仪已久的物品。\n" +
                "偶尔的放纵没关系，记得在其他月份稍微\"收敛\"一点哦。",
                formatMonthName(month),
                formatAmount(monthlyExpense.intValue()),
                (ratio - 1) * 100
            );
        } else if (ratio < 0.8) {
            return String.format(
                "%s你的消费控制得很好，支出只有%s元，比月均低了%.0f%%。\n" +
                "这种节制的消费习惯值得表扬，继续保持这种理性的理财方式！",
                formatMonthName(month),
                formatAmount(monthlyExpense.intValue()),
                (1 - ratio) * 100
            );
        } else {
            return String.format(
                "%s你的消费比较平稳，支出%s元，与月均支出基本持平。\n" +
                "%s是你最主要的支出类别，占了很大的比例。",
                formatMonthName(month),
                formatAmount(monthlyExpense.intValue()),
                peakCategory != null ? peakCategory : "餐饮美食"
            );
        }
    }

    /**
     * 生成年度对比文案
     */
    public String generateComparisonCopy(YearlyComparisonData comparison) {
        StringBuilder sb = new StringBuilder();

        sb.append("相比去年，你的财务状况");

        if (comparison.incomeGrowthRate() != null && comparison.incomeGrowthRate() > 0) {
            sb.append(String.format("更加健康。收入稳步增长%.0f%%，", comparison.incomeGrowthRate() * 100));
        } else {
            sb.append("基本稳定。");
        }

        if (comparison.expenseGrowthRate() != null) {
            if (comparison.expenseGrowthRate() < 0) {
                sb.append(String.format("同时成功控制了支出，下降了%.0f%%。",
                    Math.abs(comparison.expenseGrowthRate()) * 100));
            } else if (comparison.expenseGrowthRate() > 0) {
                sb.append(String.format("支出增长了%.0f%%，需要注意控制消费。",
                    comparison.expenseGrowthRate() * 100));
            }
        }

        sb.append("\n\n");

        if (comparison.savingsRateChange() != null) {
            if (comparison.savingsRateChange() > 0) {
                sb.append(String.format("储蓄率从去年的%.0f%%提升到了%.0f%%，这是一个非常值得骄傲的进步！",
                    comparison.lastYear().lastYearSavingsRate() * 100,
                    comparison.lastYear().lastYearSavingsRate() * 100 + comparison.savingsRateChange() * 100));
            } else {
                sb.append("储蓄率有所下降，建议明年适当增加储蓄比例。");
            }
        }

        sb.append("继续保持这种良好的理财习惯，你的财务自由之路会越走越宽。");

        return sb.toString();
    }

    /**
     * 生成分类洞察文案
     */
    public String generateCategoryInsight(
            String categoryName,
            Double categoryAmount,
            Double totalExpense,
            Double percentage
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("【%s】\n", categoryName));
        sb.append(String.format("全年支出：%s元，占总支出的%.1f%%。\n\n",
            formatAmount(categoryAmount.intValue()),
            percentage * 100));

        if (percentage > 0.3) {
            sb.append("这是你最大的支出类别，占比很高。");

            if ("餐饮美食".equals(categoryName)) {
                sb.append("说明你很注重生活品质，喜欢用美食来犒劳自己。\n");
                sb.append("建议可以适当增加在家做饭的频率，既健康又能节省开支。");
            } else if ("购物消费".equals(categoryName)) {
                sb.append("购物是你消费的重点，建议理性消费，避免冲动购物。\n");
                sb.append("可以制定购物清单，按需购买，避免不必要的浪费。");
            }
        } else if (percentage > 0.15) {
            sb.append(String.format("这是你第二大支出类别，占比%.0f%%。", percentage * 100));
            sb.append("这方面的消费比较稳定，继续保持。");
        } else {
            sb.append("这方面的支出较少，说明你在这方面的消费比较理性。");
        }

        return sb.toString();
    }

    /**
     * 生成收入分析文案
     */
    public String generateIncomeInsight(
            Double salaryPercentage,
            Double investmentPercentage,
            Double otherPercentage
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("收入构成分析\n\n");

        if (salaryPercentage > 0.8) {
            sb.append(String.format("你的收入来源比较单一，%.0f%%的收入来自工资。\n", salaryPercentage * 100));
            sb.append("这意味着你的财务状况很大程度上依赖于工作稳定性。\n");
            sb.append("建议适当增加理财投资的比例，让钱为你\"赚钱\"，\n");
            sb.append("逐步实现收入来源的多元化，降低单一收入来源带来的风险。");
        } else if (salaryPercentage > 0.6) {
            sb.append("你的收入构成比较合理，工资收入占比较大，这是大多数人的状态。\n");
            sb.append("建议继续优化收入结构，逐步增加投资理财的比例。");
        } else {
            sb.append("恭喜你，收入来源已经比较多元化了！\n");
            sb.append("这说明你在理财方面已经有一定的心得，继续保持这种良好的投资习惯。");
        }

        return sb.toString();
    }

    /**
     * 生成用户标签
     */
    public List<String> generateUserTags(
            Double avgTransactionAmount,
            Double diningPercentage,
            Double shoppingPercentage,
            Integer weekendAvg,
            Integer weekdayAvg,
            String peakHour
    ) {
        List<String> tags = new ArrayList<>();

        // 根据消费特征生成标签
        if (diningPercentage != null && diningPercentage > 0.25) {
            tags.add("餐饮达人");
        }

        if (shoppingPercentage != null && shoppingPercentage > 0.2) {
            tags.add("购物达人");
        }

        if (weekendAvg != null && weekdayAvg != null && weekendAvg > weekdayAvg * 1.3) {
            tags.add("周末派");
        }

        if (avgTransactionAmount != null) {
            if (avgTransactionAmount < 50) {
                tags.add("精打细算");
            } else if (avgTransactionAmount > 200) {
                tags.add("品质生活");
            } else {
                tags.add("理性消费");
            }
        }

        if ("20:00-23:00".equals(peakHour)) {
            tags.add("夜猫子");
        }

        // 确保至少有3个标签
        while (tags.size() < 3) {
            tags.add("理性消费");
            if (tags.size() >= 3) break;
        }

        return tags;
    }

    /**
     * 生成用户画像类型
     */
    public String generatePersonaType(List<String> tags) {
        if (tags.contains("餐饮达人") && tags.contains("夜猫子")) {
            return "夜猫子美食家";
        } else if (tags.contains("购物达人")) {
            return "理性购物者";
        } else if (tags.contains("品质生活")) {
            return "品质生活家";
        } else if (tags.contains("精打细算")) {
            return "理财小能手";
        } else {
            return "稳健理财者";
        }
    }

    // 辅助方法
    private String formatAmount(Integer amount) {
        if (amount == null) return "0";
        return String.format("%,d", amount);
    }

    private String formatMonthName(String monthStr) {
        if (monthStr == null || monthStr.length() < 7) return monthStr;

        try {
            // monthStr 格式: "2024-01"，需要提取 "01"
            String month = monthStr.substring(5, 7); // "01"
            // 去掉可能的前导零
            int monthNum = Integer.parseInt(month);
            String[] months = {"一月", "二月", "三月", "四月", "五月", "六月",
                              "七月", "八月", "九月", "十月", "十一月", "十二月"};
            if (monthNum >= 1 && monthNum <= 12) {
                return months[monthNum - 1];
            }
        } catch (Exception e) {
            // 解析失败，返回原字符串
        }
        return monthStr;
    }
}
