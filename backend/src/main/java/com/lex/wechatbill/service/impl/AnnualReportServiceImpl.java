package com.lex.wechatbill.service.impl;

import com.lex.wechatbill.entity.BillRecordEntity;
import com.lex.wechatbill.mapper.BillRecordMapper;
import com.lex.wechatbill.service.AnnualReportCopywriterService;
import com.lex.wechatbill.service.AnnualReportService;
import com.lex.wechatbill.vo.*;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;
import javax.sql.DataSource;

/**
 * 年度账本服务实现
 */
@Service
public class AnnualReportServiceImpl implements AnnualReportService {

    private static final String INCOME = "收入";
    private static final String EXPENSE = "支出";

    private final DataSource dataSource;
    private final BillRecordMapper billRecordMapper;
    private final AnnualReportCopywriterService copywriterService;

    public AnnualReportServiceImpl(
            DataSource dataSource,
            BillRecordMapper billRecordMapper,
            AnnualReportCopywriterService copywriterService
    ) {
        this.dataSource = dataSource;
        this.billRecordMapper = billRecordMapper;
        this.copywriterService = copywriterService;
    }

    @Override
    public AnnualReportVO getAnnualReport(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        String yearStr = String.valueOf(year);

        return new AnnualReportVO(
                year,
                // 基础财务数据
                buildSummaryData(yearStr),

                // 年度对比数据
                buildYearlyComparisonData(yearStr),

                // 月度统计数据
                buildMonthlyStats(yearStr),

                // 分类统计数据
                buildCategoryStats(yearStr),

                // 收入来源数据
                buildIncomeSources(yearStr),

                // 消费来源数据
                buildPaymentSources(yearStr),

                // 消费习惯数据
                buildSpendingHabitsData(yearStr),

                // 多年度趋势数据
                buildMultiYearTrendData(yearStr),

                // 智能洞察
                buildInsightsData(year)
        );
    }

    @Override
    public List<Integer> getAvailableYears() {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT substr(trade_time, 1, 4) as year FROM bill_record " +
                     "WHERE trade_time IS NOT NULL " +
                     "ORDER BY year DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    try {
                        years.add(Integer.parseInt(rs.getString("year")));
                    } catch (NumberFormatException e) {
                        // 忽略无效的年份
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取可用年份失败", e);
        }

        return years;
    }

    @Override
    public byte[] generatePdfReport(Integer year) {
        // TODO: 实现PDF生成功能
        // 可以使用 iText 或其他 PDF 库
        throw new UnsupportedOperationException("PDF生成功能待实现");
    }

    /**
     * 构建年度基础财务数据
     */
    private AnnualSummaryData buildSummaryData(String yearStr) {
        Integer totalIncome = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND settlement_included = 1 AND amount > 0",
            yearStr, INCOME
        );

        Integer totalExpense = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND settlement_included = 1 AND amount > 0",
            yearStr, EXPENSE
        );

        Integer transactionCount = queryCount(
            "SELECT COUNT(*) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND settlement_included = 1",
            yearStr
        );

        int months = 12;
        double avgMonthlyIncome = totalIncome != null ? totalIncome / (double) months : 0;
        double avgMonthlyExpense = totalExpense != null ? totalExpense / (double) months : 0;
        double savingsRate = (totalIncome != null && totalIncome > 0)
            ? (totalIncome - (totalExpense != null ? totalExpense : 0)) / (double) totalIncome
            : 0;

        return new AnnualSummaryData(
                totalIncome != null ? totalIncome : 0,
                totalExpense != null ? totalExpense : 0,
                (totalIncome != null ? totalIncome : 0) - (totalExpense != null ? totalExpense : 0),
                transactionCount != null ? transactionCount : 0,
                avgMonthlyIncome,
                avgMonthlyExpense,
                savingsRate
        );
    }

    /**
     * 构建年度对比数据
     */
    private YearlyComparisonData buildYearlyComparisonData(String currentYearStr) {
        int currentYear = Integer.parseInt(currentYearStr);
        int lastYear = currentYear - 1;
        String lastYearStr = String.valueOf(lastYear);

        // 获取去年数据
        Integer lastYearIncome = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND settlement_included = 1 AND amount > 0",
            lastYearStr, INCOME
        );

        Integer lastYearExpense = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND settlement_included = 1 AND amount > 0",
            lastYearStr, EXPENSE
        );

        double lastYearSavingsRate = (lastYearIncome != null && lastYearIncome > 0)
            ? (lastYearIncome - (lastYearExpense != null ? lastYearExpense : 0)) / (double) lastYearIncome
            : 0;

        // 获取当前年数据
        Integer currentIncome = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND settlement_included = 1 AND amount > 0",
            currentYearStr, INCOME
        );

        Integer currentExpense = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND settlement_included = 1 AND amount > 0",
            currentYearStr, EXPENSE
        );

        double currentSavingsRate = (currentIncome != null && currentIncome > 0)
            ? (currentIncome - (currentExpense != null ? currentExpense : 0)) / (double) currentIncome
            : 0;

        // 计算增长率
        Double incomeGrowthRate = null;
        if (lastYearIncome != null && lastYearIncome > 0 && currentIncome != null) {
            incomeGrowthRate = (currentIncome - lastYearIncome) / (double) lastYearIncome;
        }

        Double expenseGrowthRate = null;
        if (lastYearExpense != null && lastYearExpense > 0 && currentExpense != null) {
            expenseGrowthRate = (currentExpense - lastYearExpense) / (double) lastYearExpense;
        }

        Double savingsRateChange = null;
        if (currentSavingsRate > 0 && lastYearSavingsRate > 0) {
            savingsRateChange = currentSavingsRate - lastYearSavingsRate;
        }

        return new YearlyComparisonData(
                new LastYearData(lastYearIncome, lastYearExpense, lastYearSavingsRate),
                incomeGrowthRate,
                expenseGrowthRate,
                savingsRateChange
        );
    }

    /**
     * 构建月度统计数据
     */
    private List<MonthlyStatVO> buildMonthlyStats(String yearStr) {
        List<MonthlyStatVO> stats = new ArrayList<>();
        Map<String, MonthlyStatVO> monthMap = new HashMap<>();

        String sql = "SELECT " +
                "substr(trade_time, 1, 7) month, " +
                "CAST(coalesce(SUM(CASE WHEN income_expense_type = '收入' THEN amount ELSE 0 END), 0) AS NUMERIC) AS income_amount, " +
                "CAST(coalesce(SUM(CASE WHEN income_expense_type = '支出' THEN amount ELSE 0 END), 0) AS NUMERIC) AS expense_amount, " +
                "COUNT(*) AS transaction_count, " +
                "CAST(AVG(amount) AS NUMERIC) AS avg_amount " +
                "FROM bill_record " +
                "WHERE substr(trade_time, 1, 4) = ? AND settlement_included = 1 AND amount > 0 " +
                "GROUP BY substr(trade_time, 1, 7)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, yearStr);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String month = rs.getString("month");
                    double income = rs.getDouble("income_amount");
                    double expense = rs.getDouble("expense_amount");
                    int count = rs.getInt("transaction_count");
                    double avgAmount = rs.getDouble("avg_amount");

                    monthMap.put(month, new MonthlyStatVO(
                            month,
                            income,
                            expense,
                            income - expense,
                            count,
                            avgAmount,
                            null // peakCategory 将在后面设置
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("构建月度统计数据失败", e);
        }

        // 确保返回12个月的数据
        for (int m = 1; m <= 12; m++) {
            String monthKey = yearStr + "-" + (m < 10 ? "0" + m : String.valueOf(m));
            MonthlyStatVO stat = monthMap.get(monthKey);
            if (stat != null) {
                stats.add(stat);
            }
        }

        // 为每月设置主要支出类别
        setPeakCategoryForMonths(stats, yearStr);

        return stats;
    }

    /**
     * 构建分类统计数据
     */
    private List<CategoryStatVO> buildCategoryStats(String yearStr) {
        List<CategoryStatVO> stats = new ArrayList<>();
        Double totalExpense = getTotalExpense(yearStr);

        String sql = "SELECT " +
                "c.name AS category_name, " +
                "CAST(coalesce(SUM(b.amount), 0) AS NUMERIC) AS amount, " +
                "COUNT(*) AS transaction_count, " +
                "CAST(AVG(b.amount) AS NUMERIC) AS avg_amount " +
                "FROM bill_record b " +
                "LEFT JOIN bill_category c ON b.category_id = c.id " +
                "WHERE substr(b.trade_time, 1, 4) = ? " +
                "AND b.income_expense_type = ? " +
                "AND b.settlement_included = 1 AND b.amount > 0 " +
                "GROUP BY c.name " +
                "ORDER BY amount DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, yearStr);
            statement.setString(2, EXPENSE);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String categoryName = rs.getString("category_name");
                    double amount = rs.getDouble("amount");
                    int count = rs.getInt("transaction_count");
                    double avgAmount = rs.getDouble("avg_amount");
                    double percentage = totalExpense > 0 ? amount / totalExpense : 0;

                    stats.add(new CategoryStatVO(
                            categoryName,
                            amount,
                            percentage,
                            count,
                            avgAmount,
                            getTopProducts(yearStr, categoryName, 5)
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("构建分类统计数据失败", e);
        }

        return stats;
    }

    /**
     * 构建收入来源数据
     */
    private List<IncomeSourceVO> buildIncomeSources(String yearStr) {
        List<IncomeSourceVO> sources = new ArrayList<>();
        Double totalIncome = getTotalIncome(yearStr);

        // 工资收入（大部分收入来源）
        Integer salaryIncome = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND (counterparty LIKE '%工资%' OR counterparty LIKE '%薪水%' OR counterparty LIKE '%奖金%') " +
            "AND settlement_included = 1 AND amount > 0",
            yearStr, INCOME
        );

        // 理财收益
        Integer investmentIncome = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND (counterparty LIKE '%理财%' OR counterparty LIKE '%收益%' OR counterparty LIKE '%利息%') " +
            "AND settlement_included = 1 AND amount > 0",
            yearStr, INCOME
        );

        // 其他收入
        Integer otherIncome = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND NOT (counterparty LIKE '%工资%' OR counterparty LIKE '%薪水%' OR counterparty LIKE '%奖金%' " +
            "OR counterparty LIKE '%理财%' OR counterparty LIKE '%收益%' OR counterparty LIKE '%利息%') " +
            "AND settlement_included = 1 AND amount > 0",
            yearStr, INCOME
        );

        if (salaryIncome != null && salaryIncome > 0) {
            sources.add(new IncomeSourceVO(
                    "工资收入",
                    salaryIncome.doubleValue(),
                    totalIncome > 0 ? salaryIncome / totalIncome : 0,
                    countTransactions(yearStr, INCOME, null),
                    "稳定"
            ));
        }

        if (investmentIncome != null && investmentIncome > 0) {
            sources.add(new IncomeSourceVO(
                    "理财收益",
                    investmentIncome.doubleValue(),
                    totalIncome > 0 ? investmentIncome / totalIncome : 0,
                    countTransactions(yearStr, INCOME, "理财"),
                    "增长"
            ));
        }

        if (otherIncome != null && otherIncome > 0) {
            sources.add(new IncomeSourceVO(
                    "其他收入",
                    otherIncome.doubleValue(),
                    totalIncome > 0 ? otherIncome / totalIncome : 0,
                    countTransactions(yearStr, INCOME, null) - countTransactions(yearStr, INCOME, "理财"),
                    "稳定"
            ));
        }

        return sources;
    }

    /**
     * 构建消费来源数据（微信/支付宝）
     */
    private List<PaymentSourceVO> buildPaymentSources(String yearStr) {
        List<PaymentSourceVO> sources = new ArrayList<>();
        Double totalExpense = getTotalExpense(yearStr);

        // 微信消费
        Integer wechatAmount = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND source = '微信' AND settlement_included = 1 AND amount > 0",
            yearStr, EXPENSE
        );
        Integer wechatCount = queryCount(
            "SELECT COUNT(*) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND source = '微信' AND settlement_included = 1 AND amount > 0",
            yearStr, EXPENSE
        );

        // 支付宝消费
        Integer alipayAmount = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND source = '支付宝' AND settlement_included = 1 AND amount > 0",
            yearStr, EXPENSE
        );
        Integer alipayCount = queryCount(
            "SELECT COUNT(*) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND source = '支付宝' AND settlement_included = 1 AND amount > 0",
            yearStr, EXPENSE
        );

        if (wechatAmount != null && wechatAmount > 0) {
            sources.add(new PaymentSourceVO(
                    "微信",
                    wechatAmount.doubleValue(),
                    wechatCount,
                    totalExpense > 0 ? wechatAmount / totalExpense : 0
            ));
        }

        if (alipayAmount != null && alipayAmount > 0) {
            sources.add(new PaymentSourceVO(
                    "支付宝",
                    alipayAmount.doubleValue(),
                    alipayCount,
                    totalExpense > 0 ? alipayAmount / totalExpense : 0
            ));
        }

        return sources;
    }

    /**
     * 构建消费习惯数据
     */
    private SpendingHabitsData buildSpendingHabitsData(String yearStr) {
        // 生成用户标签
        double avgAmount = getAvgTransactionAmount(yearStr);
        double diningPercentage = getCategoryPercentage(yearStr, "餐饮");
        double shoppingPercentage = getCategoryPercentage(yearStr, "购物");

        Map<String, Object> hourStats = getHourlyStats(yearStr);
        String peakHour = (String) hourStats.get("peakHour");

        Map<String, Object> dayOfWeekStats = getDayOfWeekStats(yearStr);
        Integer weekendAvg = (Integer) dayOfWeekStats.get("weekendAvg");
        Integer weekdayAvg = (Integer) dayOfWeekStats.get("weekdayAvg");
        Double weekendRatio = (Double) dayOfWeekStats.get("weekendRatio");

        // 生成用户标签和画像
        List<String> tags = copywriterService.generateUserTags(
                avgAmount, diningPercentage, shoppingPercentage,
                weekendAvg, weekdayAvg, peakHour
        );

        String personaType = copywriterService.generatePersonaType(tags);

        String personaDescription = copywriterService.generatePersonaCopy(
                personaType, tags, avgAmount, weekendAvg, weekdayAvg
        );

        return new SpendingHabitsData(
                personaType,
                tags,
                personaDescription,
                new PeakHourData(
                        peakHour != null ? peakHour : "20:00-23:00",
                        "周六",
                        0.3, // 默认值
                        weekendRatio != null ? weekendRatio : 1.5
                ),
                new DayOfWeekData(
                        weekdayAvg != null ? weekdayAvg : 200,
                        weekendAvg != null ? weekendAvg : 300,
                        weekendRatio != null ? weekendRatio : 1.5
                )
        );
    }

    /**
     * 构建多年度趋势数据
     */
    private MultiYearTrendData buildMultiYearTrendData(String currentYearStr) {
        int currentYear = Integer.parseInt(currentYearStr);
        List<YearlyTrendItem> trends = new ArrayList<>();

        // 获取最近3年的数据
        for (int year = currentYear - 2; year <= currentYear; year++) {
            String yearStr = String.valueOf(year);
            Integer income = querySum(
                "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
                "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
                "AND settlement_included = 1 AND amount > 0",
                yearStr, INCOME
            );

            Integer expense = querySum(
                "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
                "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
                "AND settlement_included = 1 AND amount > 0",
                yearStr, EXPENSE
            );

            if (income != null) {
                double savingsRate = (income - (expense != null ? expense : 0)) / (double) income;
                trends.add(new YearlyTrendItem(
                        year,
                        income,
                        expense != null ? expense : 0,
                        income - (expense != null ? expense : 0),
                        savingsRate
                ));
            }
        }

        // 生成趋势总结和展望
        String growthSummary = generateGrowthSummary(trends);
        String forecast = generateForecast(trends);

        return new MultiYearTrendData(trends, growthSummary, forecast);
    }

    /**
     * 构建智能洞察数据
     */
    private AnnualInsightsData buildInsightsData(Integer year) {
        String yearStr = String.valueOf(year);

        AnnualSummaryData summary = buildSummaryData(yearStr);
        YearlyComparisonData yearlyComparison = buildYearlyComparisonData(yearStr);
        SpendingHabitsData habits = buildSpendingHabitsData(yearStr);

        return copywriterService.generateInsights(year, summary, yearlyComparison, habits);
    }

    // ============ 私有辅助方法 ============

    /**
     * 查询总和（双参数）
     */
    private Integer querySum(String sql, String param1, String param2) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, param1);
            statement.setString(2, param2);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询总和（单参数）
     */
    private Integer querySumOneParam(String sql, String param) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, param);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询计数（单参数）
     */
    private Integer queryCount(String sql, String param) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, param);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询计数（双参数）
     */
    private Integer queryCount(String sql, String param1, String param2) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, param1);
            statement.setString(2, param2);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询Double值（支持3个参数）
     */
    private Double queryDouble(String sql, String param1, String param2, String param3) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, param1);
            statement.setString(2, param2);
            statement.setString(3, param3);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取年度总收入
     */
    private Double getTotalIncome(String yearStr) {
        Integer total = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND settlement_included = 1 AND amount > 0",
            yearStr, INCOME
        );
        return total != null ? total.doubleValue() : 0D;
    }

    /**
     * 获取年度总支出
     */
    private Double getTotalExpense(String yearStr) {
        Integer total = querySum(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND income_expense_type = ? " +
            "AND settlement_included = 1 AND amount > 0",
            yearStr, EXPENSE
        );
        return total != null ? total.doubleValue() : 0D;
    }

    /**
     * 获取Top商品
     */
    private List<String> getTopProducts(String yearStr, String categoryName, int limit) {
        List<String> products = new ArrayList<>();
        String sql = "SELECT product_name, COUNT(*) as count " +
                     "FROM bill_record b " +
                     "LEFT JOIN bill_category c ON b.category_id = c.id " +
                     "WHERE substr(b.trade_time, 1, 4) = ? " +
                     "AND c.name = ? AND b.settlement_included = 1 AND b.amount > 0 " +
                     "GROUP BY product_name " +
                     "ORDER BY count DESC, amount DESC " +
                     "LIMIT ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, yearStr);
            statement.setString(2, categoryName);
            statement.setInt(3, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(rs.getString("product_name"));
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
        return products;
    }

    /**
     * 为每月设置主要支出类别
     */
    private void setPeakCategoryForMonths(List<MonthlyStatVO> stats, String yearStr) {
        for (int i = 0; i < stats.size(); i++) {
            MonthlyStatVO stat = stats.get(i);
            String peakCategory = getPeakCategoryForMonth(stat.month(), yearStr);
            stats.set(i, new MonthlyStatVO(
                    stat.month(),
                    stat.income(),
                    stat.expense(),
                    stat.balance(),
                    stat.transactionCount(),
                    stat.avgTransactionAmount(),
                    peakCategory
            ));
        }
    }

    /**
     * 获取某月的主要支出类别
     */
    private String getPeakCategoryForMonth(String month, String yearStr) {
        String sql = "SELECT c.name " +
                     "FROM bill_record b " +
                     "LEFT JOIN bill_category c ON b.category_id = c.id " +
                     "WHERE substr(b.trade_time, 1, 7) = ? " +
                     "AND b.income_expense_type = ? " +
                     "AND b.settlement_included = 1 AND b.amount > 0 " +
                     "GROUP BY c.name " +
                     "ORDER BY SUM(b.amount) DESC " +
                     "LIMIT 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, month);
            statement.setString(2, EXPENSE);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
        return null;
    }

    /**
     * 获取分类占比
     */
    private double getCategoryPercentage(String yearStr, String categoryKeyword) {
        Double totalExpense = getTotalExpense(yearStr);
        if (totalExpense == 0) return 0;

        Double categoryExpenseInt = queryDouble(
            "SELECT CAST(coalesce(SUM(b.amount), 0) AS NUMERIC) FROM bill_record b " +
            "LEFT JOIN bill_category c ON b.category_id = c.id " +
            "WHERE substr(b.trade_time, 1, 4) = ? AND b.income_expense_type = ? " +
            "AND c.name LIKE ? AND b.settlement_included = 1 AND b.amount > 0",
            yearStr, EXPENSE, "%" + categoryKeyword + "%"
        );

        Double categoryExpense = categoryExpenseInt != null ? categoryExpenseInt : 0D;
        return totalExpense > 0 ? categoryExpense / totalExpense : 0;
    }

    /**
     * 获取平均交易金额
     */
    private double getAvgTransactionAmount(String yearStr) {
        String sql = "SELECT CAST(AVG(amount) AS NUMERIC) " +
                     "FROM bill_record " +
                     "WHERE substr(trade_time, 1, 4) = ? " +
                     "AND settlement_included = 1 AND amount > 0";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, yearStr);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    /**
     * 统计交易次数（带过滤条件）
     */
    private int countTransactions(String yearStr, String type, String counterpartyFilter) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND settlement_included = 1"
        );

        List<String> params = new ArrayList<>();
        params.add(yearStr);

        if (type != null) {
            sql.append(" AND income_expense_type = ?");
            params.add(type);
        }

        if (counterpartyFilter != null) {
            sql.append(" AND counterparty LIKE ?");
            params.add("%" + counterpartyFilter + "%");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取时段统计
     */
    private Map<String, Object> getHourlyStats(String yearStr) {
        Map<String, Object> result = new HashMap<>();

        // 找出消费最多的时段
        String sql = "SELECT CAST(substr(trade_time, 12, 2) AS INTEGER) AS hour, " +
                     "COUNT(*) AS count " +
                     "FROM bill_record " +
                     "WHERE substr(trade_time, 1, 4) = ? " +
                     "AND settlement_included = 1 AND amount > 0 " +
                     "GROUP BY hour " +
                     "ORDER BY count DESC " +
                     "LIMIT 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, yearStr);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int hour = rs.getInt("hour");
                    String peakHour = String.format("%02d:00-%02d:00", hour, (hour + 1) % 24);
                    result.put("peakHour", peakHour);
                }
            }
        } catch (Exception e) {
            result.put("peakHour", "20:00-23:00");
        }

        return result;
    }

    /**
     * 获取星期统计
     */
    private Map<String, Object> getDayOfWeekStats(String yearStr) {
        Map<String, Object> result = new HashMap<>();

        // 工作日平均支出
        Integer weekdayTotal = querySumOneParam(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND settlement_included = 1 AND amount > 0 " +
            "AND CAST(strftime('%w', trade_time) AS INTEGER) NOT IN (0, 6)",
            yearStr
        );
        Integer weekdayCount = queryCount(
            "SELECT COUNT(DISTINCT substr(trade_time, 1, 10)) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND settlement_included = 1 AND amount > 0 " +
            "AND CAST(strftime('%w', trade_time) AS INTEGER) NOT IN (0, 6)",
            yearStr
        );

        // 周末平均支出
        Integer weekendTotal = querySumOneParam(
            "SELECT CAST(coalesce(SUM(amount), 0) AS INTEGER) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND settlement_included = 1 AND amount > 0 " +
            "AND CAST(strftime('%w', trade_time) AS INTEGER) IN (0, 6)",
            yearStr
        );
        Integer weekendCount = queryCount(
            "SELECT COUNT(DISTINCT substr(trade_time, 1, 10)) FROM bill_record " +
            "WHERE substr(trade_time, 1, 4) = ? AND settlement_included = 1 AND amount > 0 " +
            "AND CAST(strftime('%w', trade_time) AS INTEGER) IN (0, 6)",
            yearStr
        );

        Integer weekdayAvg = (weekdayCount != null && weekdayCount > 0 && weekdayTotal != null)
            ? weekdayTotal / weekdayCount : null;
        Integer weekendAvg = (weekendCount != null && weekendCount > 0 && weekendTotal != null)
            ? weekendTotal / weekendCount : null;

        Double weekendRatio = (weekdayAvg != null && weekdayAvg > 0 && weekendAvg != null)
            ? weekendAvg / (double) weekdayAvg
            : null;

        result.put("weekdayAvg", weekdayAvg);
        result.put("weekendAvg", weekendAvg);
        result.put("weekendRatio", weekendRatio);

        return result;
    }

    /**
     * 生成增长总结
     */
    private String generateGrowthSummary(List<YearlyTrendItem> trends) {
        if (trends.isEmpty()) return "暂无足够数据进行趋势分析。";

        StringBuilder sb = new StringBuilder();
        YearlyTrendItem first = trends.get(0);
        YearlyTrendItem last = trends.get(trends.size() - 1);

        sb.append(String.format("从%d年到%d年，", first.year(), last.year()));

        // 收入增长
        if (first.income() > 0) {
            double incomeGrowth = ((double) last.income() - first.income()) / first.income();
            sb.append(String.format("收入增长了%.1f%%，", incomeGrowth * 100));
        }

        // 储蓄率提升
        if (last.savingsRate() > first.savingsRate()) {
            double rateImprovement = (last.savingsRate() - first.savingsRate()) * 100;
            sb.append(String.format("储蓄率提升了%.1f个百分点。", rateImprovement));
        }

        return sb.toString();
    }

    /**
     * 生成展望
     */
    private String generateForecast(List<YearlyTrendItem> trends) {
        if (trends.isEmpty()) return "继续努力，让明天更美好！";

        YearlyTrendItem last = trends.get(trends.size() - 1);

        if (last.income() != null) {
            double projectedIncome = last.income() * 1.15; // 假设15%增长
            double projectedSavingsRate = last.savingsRate() * 1.1; // 假设储蓄率提升10%

            return String.format(
                "按照这个趋势，明年你的收入有望突破%s，储蓄率也有望提升到%d%%以上。继续保持，财务自由的目标越来越近了！",
                formatAmount((int) projectedIncome),
                (int) (projectedSavingsRate * 100)
            );
        }

        return "继续保持良好的理财习惯，让明天更美好！";
    }

    /**
     * 格式化金额
     */
    private String formatAmount(Integer amount) {
        if (amount == null) return "0";
        return String.format("%,d", amount);
    }
}
