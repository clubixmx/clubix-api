package com.clubix.api.infra.persistence.redis.store;


import com.clubix.api.flows.account.balance.CheckBalanceState;
import com.clubix.api.flows.account.balance.store.CheckBalanceStateStore;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Profile("aws")
@Service
public class ReactiveCheckBalanceStateStore implements CheckBalanceStateStore {

    private static final String PREFIX = "check-balance:";
    private static final Duration TTL = Duration.ofMinutes(15);

    private final ReactiveRedisTemplate<String, CheckBalanceState> redis;

    public ReactiveCheckBalanceStateStore(ReactiveRedisTemplate<String, CheckBalanceState> redis) {
        this.redis = redis;
    }

    public Mono<CheckBalanceState> get(String conversationId) {
        return redis.opsForValue()
                .get(key(conversationId))
                .defaultIfEmpty(CheckBalanceState.empty());
    }

    public Mono<Boolean> put(String conversationId, CheckBalanceState state) {
        return redis.opsForValue().set(key(conversationId), state, TTL);
    }

    public Mono<Boolean> exists(String conversationId) {
        return redis.hasKey(key(conversationId));
    }

    public Mono<Boolean> clear(String conversationId) {
        return redis.delete(key(conversationId))
                .map(deleted -> deleted != null && deleted > 0);
    }

    private String key(String conversationId) {
        return PREFIX + conversationId;
    }
}