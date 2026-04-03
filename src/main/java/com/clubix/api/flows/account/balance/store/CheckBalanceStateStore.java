package com.clubix.api.flows.account.balance.store;

import com.clubix.api.flows.account.balance.CheckBalanceState;
import reactor.core.publisher.Mono;

public interface CheckBalanceStateStore {
    Mono<CheckBalanceState> get(String conversationId);

    Mono<Boolean> put(String conversationId, CheckBalanceState state);

    Mono<Boolean> clear(String conversationId);

    Mono<Boolean> exists(String conversationId);
}
