package com.clubix.api.flows.core;

import reactor.core.publisher.Mono;

public interface ReactiveToolFlow {

    String name();

    boolean hasIntent(String userText);

    Mono<Boolean> isActive(String conversationId);

    Mono<String> handle(String conversationId, String userText);
}