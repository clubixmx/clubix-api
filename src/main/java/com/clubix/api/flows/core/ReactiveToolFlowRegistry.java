package com.clubix.api.flows.core;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ReactiveToolFlowRegistry {

    private final List<ReactiveToolFlow> flows;

    public ReactiveToolFlowRegistry(List<ReactiveToolFlow> flows) {
        this.flows = flows;
    }

    public Mono<String> route(String conversationId, String userText) {
        // 1) Intent match (sin IO)
        for (ReactiveToolFlow flow : flows) {
            if (flow.hasIntent(userText)) {
                return flow.handle(conversationId, userText);
            }
        }

        // 2) Active flow match (IO: Redis)
        return Flux.fromIterable(flows)
                .flatMap(flow -> flow.isActive(conversationId)
                        .filter(Boolean::booleanValue)
                        .map(active -> flow))
                .next()
                .flatMap(flow -> flow.handle(conversationId, userText));
    }
}