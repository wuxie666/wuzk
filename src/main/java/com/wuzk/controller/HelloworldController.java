package com.wuzk.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/helloworld")
public class HelloworldController {
  private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！如果用户提出涉及数据库的操作，直接调用提供的工具方法完成任务。";

  private final ChatClient dashScopeChatClient;


  public HelloworldController(JdbcTemplate jdbcTemplate, ChatClient.Builder chatClientBuilder, UserController userController) {

    // 构造 ChatMemoryRepository 和 ChatMemory ,配置 MySQL 作为聊天记忆存储
    ChatMemoryRepository chatMemoryRepository = MysqlChatMemoryRepository.mysqlBuilder()
            .jdbcTemplate(jdbcTemplate)
            .build();
    ChatMemory chatMemory = MessageWindowChatMemory.builder()
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
   * @param query 用户问题
   * @param chatId 对话ID，用于上下文记忆
   * @return JSON 格式的回复
   */
  @GetMapping("/chat")
  public Map<String, Object> chat(
          @RequestParam(value = "query") String query,
          @RequestParam(value = "chat-id", defaultValue = "1") String chatId) {

    // 调用大模型
    String reply = dashScopeChatClient.prompt(query)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
            .call()
            .content();

    // 组装结构化返回
    Map<String, Object> result = new HashMap<>();
    result.put("status", "success");
    result.put("query", query);
    result.put("reply", reply);
    result.put("chatId", chatId);
    return result;
  }
}