package com.wuzk.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/helloworld")
public class HelloworldController {
  private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

  private final ChatClient dashScopeChatClient;


  public HelloworldController(JdbcTemplate jdbcTemplate, ChatClient.Builder chatClientBuilder, UserController userController) {

    // 构造 ChatMemoryRepository 和 ChatMemory
    ChatMemoryRepository chatMemoryRepository = MysqlChatMemoryRepository.mysqlBuilder()
            .jdbcTemplate(jdbcTemplate)
            .build();
    ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .build();

    this.dashScopeChatClient = chatClientBuilder
        .defaultSystem(DEFAULT_PROMPT)
        // 实现 Logger 的 Advisor
        .defaultAdvisors(
            new SimpleLoggerAdvisor()
        )
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
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
  * ChatClient 简单调用
  */
  @GetMapping("/simple/chat")
  public String simpleChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？")String query,
                           @RequestParam(value = "chat-id", defaultValue = "1") String chatId) {

    return dashScopeChatClient.prompt(query)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
            .call().content();
  }
}