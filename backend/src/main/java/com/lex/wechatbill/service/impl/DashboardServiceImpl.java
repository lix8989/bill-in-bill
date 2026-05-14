package com.lex.wechatbill.service.impl;

import com.lex.wechatbill.service.DashboardService;
import com.lex.wechatbill.vo.DashboardCategoryVO;
import com.lex.wechatbill.vo.DashboardDayVO;
import com.lex.wechatbill.vo.DashboardMonthVO;
import com.lex.wechatbill.vo.DashboardSourceVO;
import com.lex.wechatbill.vo.DashboardTopItemVO;
import com.lex.wechatbill.vo.DashboardVO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final String INCOME = "收入";
    private static final String EXPENSE = "支出";

    private final DataSource dataSource;

    public DashboardServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DashboardVO getDashboard(Integer year, String month) {
        String selectedYear = year == null ? currentYear() : String.valueOf(year);
        String selectedMonth = (month == null || month.isBlank()) ? currentMonth() : month;
        return new DashboardVO(
            Integer.valueOf(selectedYear),
            selectedMonth,
            querySum("select cast(coalesce(sum(amount), 0) as integer) from bill_record where substr(trade_time, 1, 4) = ? and income_expense_type = ? and settlement_included = 1 and amount > 0", selectedYear, INCOME),
            querySum("select cast(coalesce(sum(amount), 0) as integer) from bill_record where substr(trade_time, 1, 4) = ? and income_expense_type = ? and settlement_included = 1 and amount > 0", selectedYear, EXPENSE),
            querySum("select cast(coalesce(sum(amount), 0) as integer) from bill_record where substr(trade_time, 1, 7) = ? and income_expense_type = ? and settlement_included = 1 and amount > 0", selectedMonth, INCOME),
            querySum("select cast(coalesce(sum(amount), 0) as integer) from bill_record where substr(trade_time, 1, 7) = ? and income_expense_type = ? and settlement_included = 1 and amount > 0", selectedMonth, EXPENSE),
            queryCategoryStats("select c.name, coalesce(sum(b.amount), 0) amount from bill_record b left join bill_category c on b.category_id = c.id where substr(b.trade_time, 1, 4) = ? and b.income_expense_type = ? and b.settlement_included = 1 and b.amount > 0 group by c.name order by amount desc", selectedYear, EXPENSE),
            queryCategoryStats("select c.name, coalesce(sum(b.amount), 0) amount from bill_record b left join bill_category c on b.category_id = c.id where substr(b.trade_time, 1, 7) = ? and b.income_expense_type = ? and b.settlement_included = 1 and b.amount > 0 group by c.name order by amount desc", selectedMonth, EXPENSE),
            queryTopItems("select product_name, amount from bill_record where substr(trade_time, 1, 4) = ? and income_expense_type = ? and settlement_included = 1 and amount > 0 order by amount desc limit 10", selectedYear, EXPENSE),
            queryTopItems("select product_name, amount from bill_record where substr(trade_time, 1, 7) = ? and income_expense_type = ? and settlement_included = 1 and amount > 0 order by amount desc limit 10", selectedMonth, EXPENSE),
            queryMonthTrend(selectedYear, ""),
            querySource(selectedYear, null, INCOME),
            querySource(selectedYear, null, EXPENSE),
            querySource(selectedMonth, selectedMonth, INCOME),
            querySource(selectedMonth, selectedMonth, EXPENSE),
            queryMonthTrend(selectedYear, "微信"),
            queryMonthTrend(selectedYear, "支付宝"),
            queryCategoryStats("select c.name, coalesce(sum(b.amount), 0) amount from bill_record b left join bill_category c on b.category_id = c.id where substr(b.trade_time, 1, 4) = ? and b.income_expense_type = ? and b.settlement_included = 1 and b.amount > 0" + sourceClause("微信") + " group by c.name order by amount desc", selectedYear, EXPENSE),
            queryCategoryStats("select c.name, coalesce(sum(b.amount), 0) amount from bill_record b left join bill_category c on b.category_id = c.id where substr(b.trade_time, 1, 4) = ? and b.income_expense_type = ? and b.settlement_included = 1 and b.amount > 0" + sourceClause("支付宝") + " group by c.name order by amount desc", selectedYear, EXPENSE),
            queryDayTrend(selectedMonth)
        );
    }

    private DashboardSourceVO querySource(String yearValue, String monthValue, String type) {
        String timeCond = monthValue != null ? "substr(trade_time, 1, 7) = ?" : "substr(trade_time, 1, 4) = ?";
        String param = monthValue != null ? monthValue : yearValue;
        Integer wechat = querySum("select cast(coalesce(sum(amount), 0) as integer) from bill_record where " + timeCond + " and income_expense_type = ? and settlement_included = 1 and amount > 0 and (source is null or source = '微信')", param, type);
        Integer alipay = querySum("select cast(coalesce(sum(amount), 0) as integer) from bill_record where " + timeCond + " and income_expense_type = ? and settlement_included = 1 and amount > 0 and source = '支付宝'", param, type);
        return new DashboardSourceVO(wechat, alipay);
    }

    private String sourceClause(String source) {
        if (source == null || source.isBlank()) return "";
        if ("微信".equals(source)) return " and (b.source is null or b.source = '微信')";
        if ("支付宝".equals(source)) return " and b.source = '支付宝'";
        return "";
    }

    private List<DashboardMonthVO> queryMonthTrend(String year, String source) {
        String sourceFilter = "";
        if ("微信".equals(source)) sourceFilter = " and (source is null or source = '微信')";
        else if ("支付宝".equals(source)) sourceFilter = " and source = '支付宝'";
        String sql = "select substr(trade_time, 1, 7) month, coalesce(sum(case when income_expense_type = '收入' then amount else 0 end), 0) income_amount, coalesce(sum(case when income_expense_type = '支出' then amount else 0 end), 0) expense_amount from bill_record where substr(trade_time, 1, 4) = ? and settlement_included = 1 and amount > 0" + sourceFilter + " group by substr(trade_time, 1, 7) order by month asc";
        java.util.Map<String, DashboardMonthVO> monthMap = new java.util.HashMap<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, year);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    DashboardMonthVO vo = new DashboardMonthVO(rs.getString(1), rs.getDouble(2), rs.getDouble(3));
                    monthMap.put(rs.getString(1), vo);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        List<DashboardMonthVO> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String key = year + "-" + (m < 10 ? "0" + m : String.valueOf(m));
            DashboardMonthVO vo = monthMap.get(key);
            result.add(vo != null ? vo : new DashboardMonthVO(key, 0D, 0D));
        }
        return result;
    }

    private List<DashboardDayVO> queryDayTrend(String month) {
        String sql = "select substr(trade_time, 6, 5) day, coalesce(sum(case when income_expense_type = '收入' then amount else 0 end), 0) income_amount, coalesce(sum(case when income_expense_type = '支出' then amount else 0 end), 0) expense_amount from bill_record where substr(trade_time, 1, 7) = ? and settlement_included = 1 and amount > 0 group by substr(trade_time, 6, 5) order by day asc";
        List<DashboardDayVO> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, month);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new DashboardDayVO(rs.getString(1), rs.getDouble(2), rs.getDouble(3)));
                }
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private Integer querySum(String sql, String value1, String value2) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value1);
            statement.setString(2, value2);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private List<DashboardCategoryVO> queryCategoryStats(String sql, String value1, String value2) {
        List<DashboardCategoryVO> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value1);
            statement.setString(2, value2);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new DashboardCategoryVO(rs.getString(1), rs.getDouble(2)));
                }
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private List<DashboardTopItemVO> queryTopItems(String sql, String value1, String value2) {
        List<DashboardTopItemVO> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value1);
            statement.setString(2, value2);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new DashboardTopItemVO(rs.getString(1), rs.getDouble(2)));
                }
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private String currentYear() {
        return String.valueOf(LocalDate.now().getYear());
    }

    private String currentMonth() {
        LocalDate now = LocalDate.now();
        String value = now.getMonthValue() < 10 ? "0" + now.getMonthValue() : String.valueOf(now.getMonthValue());
        return String.valueOf(now.getYear()) + "-" + value;
    }
}
