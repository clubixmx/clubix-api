package com.clubix.api.config;

import com.clubix.api.tools.AccountTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatClientConfig {

    @Bean("toolChatClient")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, AccountTools timeTools, ChatMemoryRepository chatMemoryRepository) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20) // ventana recomendada; ajusta a tu gusto
                .build();
        return chatClientBuilder
                .defaultTools(timeTools)
                .defaultAdvisors(List.of(loggerAdvisor, MessageChatMemoryAdvisor.builder(chatMemory).build()))
                .build();
    }
}
