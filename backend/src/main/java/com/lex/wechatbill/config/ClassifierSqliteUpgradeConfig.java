package com.lex.wechatbill.config;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClassifierSqliteUpgradeConfig {

    private final DataSource dataSource;

    public ClassifierSqliteUpgradeConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void upgradeTables() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            upgradeCategory(statement);
            upgradeBillRecord(statement);
            upgradeSyncLog(statement);
            upgradeClassifyTask(statement);
            upgradeLlmSettings(statement);
            // upgradeKeywordRule(statement);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private void upgradeCategory(Statement statement) throws Exception {
        run(statement, "alter table bill_category add column category_code text");
        run(statement, "alter table bill_category add column source text default 'manual'");
        run(statement, "alter table bill_category add column enabled integer default 1");
        run(statement, "alter table bill_category add column updated_at text");
        run(statement, "alter table bill_category add column description text");
        statement.executeUpdate("update bill_category set source = 'manual' where source is null");
        statement.executeUpdate("update bill_category set enabled = 1 where enabled is null");
        statement.executeUpdate("update bill_category set updated_at = datetime('now','localtime') where updated_at is null");
        statement.executeUpdate("update bill_category set category_code = upper(replace(name, ' ', '_')) where (category_code is null or trim(category_code) = '') and name is not null");
        statement.executeUpdate("update bill_category set category_code = 'LIVING_BILL' where id = 1");
        statement.executeUpdate("update bill_category set category_code = 'TRANSPORT' where id = 2");
        statement.executeUpdate("update bill_category set category_code = 'SHOPPING' where id = 3");
        statement.executeUpdate("update bill_category set category_code = 'DINING' where id = 4");
        statement.executeUpdate("update bill_category set category_code = 'CAR_MAINTENANCE' where id = 5");
        statement.executeUpdate("update bill_category set category_code = 'RENT' where id = 6");
        statement.executeUpdate("update bill_category set category_code = 'SERVICE' where id = 7");
        statement.executeUpdate("update bill_category set category_code = 'OTHER' where id = 8");
        statement.executeUpdate("update bill_category set category_code = 'CAR_CHARGING' where id = 9");
        statement.executeUpdate("update bill_category set category_code = 'CAR_FUEL' where id = 10");
        statement.executeUpdate("update bill_category set category_code = 'RED_PACKET' where id = 11");
        statement.executeUpdate("update bill_category set category_code = 'TRANSFER' where id = 12");
        statement.executeUpdate("update bill_category set category_code = 'MEDICAL' where id = 13");
        statement.executeUpdate("update bill_category set category_code = 'TRAVEL' where id = 14");
        statement.executeUpdate("update bill_category set category_code = 'ENTERTAINMENT' where id = 15");
        statement.execute("create unique index if not exists uk_bill_category_category_code on bill_category(category_code)");
    }

    private void upgradeBillRecord(Statement statement) throws Exception {
        run(statement, "alter table bill_record add column category_confidence real");
        run(statement, "alter table bill_record add column category_source text default 'import-default'");
        run(statement, "alter table bill_record add column category_sync_status text");
        run(statement, "alter table bill_record add column category_sync_reason text");
        run(statement, "alter table bill_record add column category_sync_at text");
        statement.executeUpdate("update bill_record set category_source = 'import-default' where category_source is null");
    }

    private void upgradeSyncLog(Statement statement) throws Exception {
        statement.execute("create table if not exists bill_category_sync_log (id integer primary key autoincrement, sync_batch_no text, bill_id integer, import_key text, requested_category_code text, resolved_category_id integer, confidence real, reason text, status text, message text, created_at text default (datetime('now','localtime'))) ");
        statement.execute("create index if not exists idx_bill_category_sync_log_batch_no on bill_category_sync_log(sync_batch_no)");
    }

    private void upgradeClassifyTask(Statement statement) throws Exception {
        statement.execute("create table if not exists bill_classify_task (id integer primary key autoincrement, task_no text, task_type text, total_count integer default 0, success_count integer default 0, fail_count integer default 0, status text, error_message text, started_at text, finished_at text, created_at text default (datetime('now','localtime'))) ");
    }

    private void upgradeLlmSettings(Statement statement) throws Exception {
        statement.execute("create table if not exists llm_settings (id integer primary key autoincrement, config_key text unique not null, config_value text) ");
        statement.executeUpdate("insert or ignore into llm_settings (config_key, config_value) values ('api_url', 'https://api.openai.com/v1/chat/completions') ");
        statement.executeUpdate("insert or ignore into llm_settings (config_key, config_value) values ('api_key', '') ");
        statement.executeUpdate("insert or ignore into llm_settings (config_key, config_value) values ('model_name', 'gpt-4o-mini') ");
        statement.executeUpdate("insert or ignore into llm_settings (config_key, config_value) values ('max_tokens', '1024') ");
        statement.executeUpdate("insert or ignore into llm_settings (config_key, config_value) values ('temperature', '0.1') ");
        statement.executeUpdate("insert or ignore into llm_settings (config_key, config_value) values ('enabled', 'false') ");
    }

    private void upgradeKeywordRule(Statement statement) throws Exception {
        statement.execute("create table if not exists bill_keyword_rule (id integer primary key autoincrement, category_code text not null, category_name text, keyword text not null, confidence real default 0.85, sort_order integer default 0, enabled integer default 1, created_at text default (datetime('now','localtime')), updated_at text default (datetime('now','localtime'))) ");
        var rs = statement.executeQuery("select count(*) as cnt from bill_keyword_rule");
        boolean hasData = rs.next() && rs.getInt("cnt") > 0;
        rs.close();
        if (!hasData) {
            String[][] seed = {
                {"DINING","餐饮","0.90","咖啡,餐饮,外卖,食堂,餐厅,美食,吃饭,午餐,晚餐,早餐,瑞幸,肯德基,麦当劳,星巴克,海底捞,美团外卖,饿了么"},
                {"GROCERY","买菜生鲜","0.85","生鲜,买菜,蔬菜,水果,超市,便利店,永辉,盒马,沃尔玛,大润发,菜市场,京东到家"},
                {"SHOPPING","日用购物","0.85","购物,网购,淘宝,京东,拼多多,唯品会,天猫,商城,百货,数码,电器,服装,电商"},
                {"TRANSPORT","交通出行","0.88","滴滴,出租车,地铁,公交,交通,出行,打车,高铁,火车,飞机,机票,航旅,高德,百度地图,网约车"},
                {"LIVING_BILL","水电燃气","0.85","水电,电费,水费,燃气,煤气,物业,供暖"},
                {"TELECOM","通讯网络","0.88","话费,流量,宽带,中国移动,中国联通,中国电信,通讯"},
                {"MEDICAL","医疗健康","0.88","医院,药店,医药,药品,挂号,体检,医疗,健康,医保,诊所"},
                {"ENTERTAINMENT","娱乐休闲","0.82","电影,游戏,娱乐,视频,音乐,会员,充值,直播,腾讯视频,爱奇艺,网易云,B站,抖音"},
                {"SOCIAL","人情往来","0.80","红包,转账,礼金,随礼,份子"},
                {"FINANCE","金融理财","0.85","理财,基金,股票,保险,投资,还款,信用卡"},
                {"RENT","租房","0.90","房租,租金,租房,押金"},
                {"CAR_FUEL","汽车加油","0.92","加油,加油站,中石化,中石油,壳牌"},
                {"CAR_CHARGING","汽车充电","0.90","充电,充电桩,特来电,星星充电"},
                {"CAR_MAINTENANCE","停车/保养/车险","0.85","停车,保养,修车,洗车,车险,年检,违章,过路费"},
                {"TRANSFER","转账收付款","0.85","转账,收款,收付款"},
                {"SALARY","工资收入","0.92","工资,奖金,补贴,薪资,薪酬"},
                {"REFUND","退款退回","0.90","退款,退货,返现,退还"},
            };
            String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            for (String[] row : seed) {
                String code = row[0];
                String name = row[1];
                String conf = row[2];
                String[] keywords = row[3].split(",");
                for (int i = 0; i < keywords.length; i++) {
                    String sql = "insert into bill_keyword_rule (category_code, category_name, keyword, confidence, sort_order, enabled, created_at, updated_at) values ('"
                        + escape(code) + "','" + escape(name) + "','" + escape(keywords[i].trim()) + "'," + conf + "," + i + ",1,'" + now + "','" + now + "')";
                    statement.executeUpdate(sql);
                }
            }
        }
        // syncKeywordCategories(statement);
    }

    private void syncKeywordCategories(Statement statement) throws Exception {
        String[][] categories = {
            {"GROCERY", "买菜生鲜"},
            {"TELECOM", "通讯网络"},
            {"SOCIAL", "人情往来"},
            {"FINANCE", "金融理财"},
            {"SALARY", "工资收入"},
            {"REFUND", "退款退回"},
        };
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        var maxIdRs = statement.executeQuery("select coalesce(max(id), 0) as mid from bill_category");
        int nextId = maxIdRs.next() ? maxIdRs.getInt("mid") + 1 : 1;
        maxIdRs.close();
        for (String[] cat : categories) {
            var check = statement.executeQuery("select count(*) as cnt from bill_category where category_code = '" + escape(cat[0]) + "'");
            boolean exists = check.next() && check.getInt("cnt") > 0;
            check.close();
            if (!exists) {
                String sql = "insert into bill_category (id, name, category_code, source, enabled, updated_at) values ("
                    + (nextId++) + ",'" + escape(cat[1]) + "','" + escape(cat[0]) + "','system',1,'" + now + "')";
                statement.executeUpdate(sql);
            }
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    private void run(Statement statement, String sql) throws Exception {
        try {
            statement.execute(sql);
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message == null || (!message.contains("duplicate column name") && !message.contains("already exists"))) {
                throw ex;
            }
        }
    }
}
