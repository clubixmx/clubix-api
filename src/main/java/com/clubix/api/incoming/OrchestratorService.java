package com.clubix.api.incoming;

import com.clubix.api.flows.core.ReactiveToolFlowRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private final ReactiveToolFlowRegistry registry;

    @Qualifier("toolChatClient")
    private final ChatClient chatClient;

    public Mono<String> handle(String conversationId, String userText) {
        return registry.route(conversationId, userText)
                .switchIfEmpty(fallbackLLM(conversationId, userText));
    }

    private Mono<String> fallbackLLM(String conversationId, String userText) {
        return Mono.fromCallable(() ->
                        chatClient.prompt()
                                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                                .user(userText)
                                .call()
                                .content()
                )
                .subscribeOn(Schedulers.boundedElastic());
    }
}