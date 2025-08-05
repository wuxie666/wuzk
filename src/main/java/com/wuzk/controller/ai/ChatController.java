package com.wuzk.controller.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import com.wuzk.controller.UserController;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
public class ChatController {
  private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！如果用户提出涉及数据库的操作，直接调用提供的工具方法完成任务。";

  private final ChatClient dashScopeChatClient;


  public ChatController(JdbcTemplate jdbcTemplate, ChatClient.Builder chatClientBuilder, UserController userController) {

    // 构造 ChatMemoryRepository 和 ChatMemory ,配置 MySQL 作为聊天记忆存储
    var chatMemoryRepository = MysqlChatMemoryRepository.mysqlBuilder()
            .jdbcTemplate(jdbcTemplate)
            .build();
    var chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .build();

    // 构建 ChatClient，注册记忆、日志、工具（UserController）
    this.dashScopeChatClient = chatClientBuilder
        .defaultSystem(DEFAULT_PROMPT)
        // 实现 Logger 的 Advisor
        .defaultAdvisors(
            new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build()
        )
        .defaultTools(userController)
        // 设置 ChatClient 中 ChatModel 的 Options 参数
        .defaultOptions(
            DashScopeChatOptions.builder()
                .withTopP(0.7)
                .build()
        )
        .build();
  }

  /**
   * 智能聊天接口：支持自然语言操作数据库
   * 请求示例：
   * POST /ai/chat
   * {
   *   "query": "帮我查一下用户列表"
   * }
   */
  @PostMapping("/chat")
  public Map<String, Object> chat(@RequestBody Map<String, String> request) {
    var query = request.get("query");

    if (query == null || query.isBlank()) {
      throw new IllegalArgumentException("query 参数不能为空");
    }

    // 如果前端没有传 chat-id，后端自动生成 UUID
    String chatId = request.getOrDefault("chat-id", UUID.randomUUID().toString());

    // 调用大模型
    var reply = dashScopeChatClient.prompt(query)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
            .call()
            .content();

    // 组装结构化返回
    var result = new HashMap<String, Object>();
    result.put("status", "success");
    result.put("query", query);
    result.put("reply", reply);
    result.put("chatId", chatId);
    return result;
  }
}