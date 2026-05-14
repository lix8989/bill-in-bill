package com.lex.wechatbill.config;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SqliteSchemaUpgradeConfig {

    private final DataSource dataSource;

    public SqliteSchemaUpgradeConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void upgradeTables() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            upgradeBillRecordTable(statement);
            upgradeImportHistoryTable(statement);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private void upgradeBillRecordTable(Statement statement) throws Exception {
        Set<String> columns = readColumns(statement, \u0022bill_record\u0022);
        addColumnIfMissing(statement, columns, \u0022income_expense_type\u0022, \u0022alter table bill_record add column income_expense_type text\u0022);
        addColumnIfMissing(statement, columns, \u0022pay_method\u0022, \u0022alter table bill_record add column pay_method text\u0022);
        addColumnIfMissing(statement, columns, \u0022trade_status\u0022, \u0022alter table bill_record add column trade_status text\u0022);
        addColumnIfMissing(statement, columns, \u0022trade_no\u0022, \u0022alter table bill_record add column trade_no text\u0022);
        addColumnIfMissing(statement, columns, \u0022merchant_order_no\u0022, \u0022alter table bill_record add column merchant_order_no text\u0022);
        addColumnIfMissing(statement, columns, \u0022remark\u0022, \u0022alter table bill_record add column remark text\u0022);
        addColumnIfMissing(statement, columns, \u0022settlement_included\u0022, \u0022alter table bill_record add column settlement_included integer default 1\u0022);
        statement.executeUpdate(\u0022update bill_record set settlement_included = 1 where settlement_included is null\u0022);
        addColumnIfMissing(statement, columns, \u0022source\u0022, \u0022alter table bill_record add column source text default '微信'\u0022);
        statement.executeUpdate(\u0022update bill_record set source = '微信' where source is null\u0022);
    }

    private void upgradeImportHistoryTable(Statement statement) throws Exception {
        statement.execute(\u0022create table if not exists import_history (id integer primary key autoincrement, source_file_name text, total_count integer not null default 0, success_count integer not null default 0, fail_count integer not null default 0, message text, created_at text default (datetime('now','localtime'))) \u0022);
        Set<String> columns = readColumns(statement, \u0022import_history\u0022);
        addColumnIfMissing(statement, columns, \u0022source\u0022, \u0022alter table import_history add column source text\u0022);
    }

    private Set<String> readColumns(Statement statement, String tableName) throws Exception {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = statement.executeQuery(\u0022pragma table_info(\u0022 + tableName + \u0022)\u0022)) {
            while (rs.next()) {
                columns.add(rs.getString(\u0022name\u0022));
            }
        }
        return columns;
    }

    private void addColumnIfMissing(Statement statement, Set<String> columns, String columnName, String sql) throws Exception {
        if (!columns.contains(columnName)) {
            statement.execute(sql);
            columns.add(columnName);
        }
    }
}
