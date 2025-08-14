package com.wuzk.client;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
public class OpenAiClient {

    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;

    private List<String> tableNames;

    // 缓存表 -> 列名列表
    private final Map<String, List<String>> tableColumns = new HashMap<>();

    // 表.字段 -> 中文描述
    private final Map<String, String> columnDescriptions = new HashMap<>();

    public OpenAiClient(RestTemplate restTemplate, JdbcTemplate jdbcTemplate) {
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void loadTableNamesAndColumns() {
        tableNames = jdbcTemplate.queryForList("SHOW TABLES", String.class);
        for (String table : tableNames) {
            List<String> columns = jdbcTemplate.query(
                    "SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                    new Object[]{table},
                    (rs, rowNum) -> {
                        String name = rs.getString("COLUMN_NAME");
                        String comment = rs.getString("COLUMN_COMMENT");
                        columnDescriptions.put(table + "." + name, (comment != null && !comment.isBlank()) ? comment : name);
                        return name;
                    });
            tableColumns.put(table, columns);
        }
        log.info("✅ 数据库表和字段已加载: {}", tableColumns);
        log.info("✅ 字段中文描述已加载: {}", columnDescriptions);
    }

    public List<String> generateSql(String command) {
        if (tableNames == null || tableNames.isEmpty()) {
            throw new RuntimeException("无法获取数据库表，请检查数据库连接");
        }

        String tableList = String.join(", ", tableNames);
        String prompt = """
                你是一个MySQL专家。
                根据以下自然语言指令生成一条安全的MySQL语句。
                要求：
                1. 表名必须是以下之一：%s
                2. 自动填充 create_time = NOW() 和 update_time = NOW()（如果字段存在）
                3. 只输出SQL，不要解释
                4. 修改操作必须根据主键（id）修改
                5. 删除操作必须带 WHERE id = ?
                6. 不允许 DROP / TRUNCATE / ALTER 等危险操作
                7. 查询必须返回完整SQL，且带 LIMIT 语句（除非用户指定）
                8. SQL 必须以分号结尾

                指令：
                """.formatted(tableList) + command;

        Map<String, String> request = new HashMap<>();
        request.put("query", prompt);

        log.debug("发送给大模型的 prompt: {}", prompt);

        Map<String, Object> response = restTemplate.postForObject(
                "http://localhost:8080/ai/chat", request, Map.class);

        if (response == null || !response.containsKey("reply") || !(response.get("reply") instanceof String)) {
            log.error("AI 服务返回异常: {}", response);
            throw new RuntimeException("AI 服务未返回有效响应");
        }

        String reply = ((String) response.get("reply")).trim();
        if (reply.isEmpty()) {
            log.error("大模型未返回有效 SQL");
            throw new RuntimeException("AI 服务未返回有效响应");
        }

        log.debug("大模型返回: {}", reply);

        List<String> sqlList = extractSqlList(reply);
        for (int i = 0; i < sqlList.size(); i++) {
            String sql = sqlList.get(i);
            checkDangerousSql(sql);
            sql = autoReplaceIdPlaceholder(sql);
            log.info("生成 SQL: {}", sql);
            sqlList.set(i, sql);
        }

        return sqlList;
    }

    private List<String> extractSqlList(String reply) {
        List<String> result = new ArrayList<>();
        if (reply == null) return result;

        String cleaned = reply.replaceAll("```.*?```", "").trim();
        String[] statements = cleaned.split(";");

        for (String stmt : statements) {
            String sql = stmt.trim();
            if (!sql.isEmpty()) {
                sql = autoFillTimestamps(sql);
                result.add(sql + ";");
            }
        }
        return result;
    }

    private String autoFillTimestamps(String sql) {
        String upper = sql.toUpperCase();
        if (upper.startsWith("INSERT")) {
            if (!upper.contains("CREATE_TIME")) {
                sql = sql.replaceFirst("\\)$", ", create_time, update_time)");
                sql = sql.replaceFirst("\\)$", ", NOW(), NOW())");
            }
        } else if (upper.startsWith("UPDATE")) {
            if (!upper.contains("UPDATE_TIME")) {
                sql = sql.replaceFirst("SET", "SET update_time = NOW(), ");
            }
        }
        return sql;
    }

    private void checkDangerousSql(String sql) {
        String upper = sql.toUpperCase();
        if (upper.matches(".*\\b(DROP|TRUNCATE|ALTER)\\b.*")) {
            log.error("检测到危险 SQL: {}", sql);
            throw new RuntimeException("检测到危险 SQL: " + sql);
        }
    }

    private String autoReplaceIdPlaceholder(String sql) {
        String upper = sql.toUpperCase();
        if ((upper.startsWith("DELETE") || upper.startsWith("UPDATE")) && sql.contains("?")) {
            sql = sql.replaceAll("\\?", "1");
        }
        return sql;
    }
}
