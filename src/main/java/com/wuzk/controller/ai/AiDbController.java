package com.wuzk.controller.ai;

import com.wuzk.service.ai.AiDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai/db")
public class AiDbController {

    @Autowired
    private AiDbService aiDbService;

    /**
     * 操作数据库
     * @param request
     * @return
     */
    @PostMapping("/execute")
    public Map<String, Object> execute(@RequestBody Map<String, String> request) {
        String query = request.get("query"); // 自然语言指令
        return aiDbService.executeAiSql(query);
    }
}
