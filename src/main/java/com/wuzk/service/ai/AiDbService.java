package com.wuzk.service.ai;

import com.wuzk.client.OpenAiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiDbService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OpenAiClient openAiClient;

    public Map<String, Object> executeAiSql(String userCommand) {
        // 1. 生成SQL
        var sql = openAiClient.generateSql(userCommand);

        // 2. SQL安全检查
        if (!isSafeSql(sql)) {
            throw new RuntimeException("SQL 非法或危险，已拦截：" + sql);
        }

        var result = new HashMap<String, Object>();
        result.put("sql", sql);

        // 3. 判断SQL类型
        var lower = sql.trim().toLowerCase();
        if (lower.startsWith("select")) {
            // 查询
            result.put("status", "success");
            result.put("data", jdbcTemplate.queryForList(sql));
        } else {
            // 更新或插入
            var rows = jdbcTemplate.update(sql);
            result.put("status", "success");
            result.put("rowsAffected", rows);
        }

        return result;
    }

    /** 简单SQL安全校验 */
    private boolean isSafeSql(String sql) {
        if (sql == null) return false;
        var lower = sql.trim().toLowerCase();
        return lower.startsWith("insert") || lower.startsWith("update")
                || lower.startsWith("select") || lower.startsWith("delete");
    }
}
