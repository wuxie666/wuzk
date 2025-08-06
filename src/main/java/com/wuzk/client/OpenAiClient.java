package com.wuzk.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class OpenAiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用大模型生成SQL
     */
    public String generateSql(String command) {
        // 提示词设计
        var prompt = """
                        你是一个MySQL专家。
                        根据以下自然语言指令生成一条安全的MySQL语句。
                        
                        要求：
                        1. 表名为 user
                        2. 自动填充 create_time = NOW()
                        3. 只输出SQL，不要解释
                        4. 如果是查询，需要返回完整SQL
                        
                        指令：
                        """ + command;


        var request = new HashMap<String, String>();
        request.put("query", prompt);

        // 调用你原来的大模型接口（假设返回JSON里有"reply"字段）
        var response = restTemplate.postForObject(
                "http://localhost:8080/ai/chat", request, Map.class);

        var reply = (String) response.get("reply");
        return extractSql(reply);
    }

    /**
     * 提取SQL（去掉AI可能带的解释或换行）
     */
    private String extractSql(String reply) {
        if (reply == null) return "";
        // 取第一行分号结尾或整段文本
        var cleaned = reply.trim();
        var idx = cleaned.indexOf(";");
        if (idx != -1) {
            return cleaned.substring(0, idx + 1);
        }
        return cleaned;
    }
}
