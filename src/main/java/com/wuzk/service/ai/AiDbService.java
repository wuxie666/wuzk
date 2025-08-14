package com.wuzk.service.ai;

import com.wuzk.client.OpenAiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiDbService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OpenAiClient openAiClient;

    /**
     * 执行用户指令生成的 SQL（支持多条 SQL）
     */
    public Map<String, Object> executeAiSql(String userCommand) {
        // 1. 生成 SQL 列表
        List<String> sqlList = openAiClient.generateSql(userCommand);

        if (sqlList.isEmpty()) {
            throw new RuntimeException("未生成任何 SQL");
        }

        var result = new HashMap<String, Object>();
        result.put("sql", sqlList);

        List<Map<String, Object>> queryResults = new ArrayList<>();
        int totalRowsAffected = 0;

        for (String sql : sqlList) {
            // 2. SQL 安全检查
            if (!isSafeSql(sql)) {
                throw new RuntimeException("SQL 非法或危险，已拦截：" + sql);
            }

            // 3. 判断 SQL 类型
            var lower = sql.trim().toLowerCase();
            if (lower.startsWith("select")) {
                // 查询
                queryResults.addAll(jdbcTemplate.queryForList(sql));
            } else {
                // 更新或插入
                int rows = jdbcTemplate.update(sql);
                totalRowsAffected += rows;
            }
        }

        result.put("status", "success");
        if (!queryResults.isEmpty()) {
            result.put("data", queryResults);
        }
        if (totalRowsAffected > 0) {
            result.put("rowsAffected", totalRowsAffected);
        }

        return result;
    }

    /** 简单 SQL 安全校验 */
    private boolean isSafeSql(String sql) {
        if (sql == null) return false;
        var lower = sql.trim().toLowerCase();
        return lower.startsWith("insert") || lower.startsWith("update")
                || lower.startsWith("select") || lower.startsWith("delete");
    }
}
