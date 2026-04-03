package com.clubix.api.infra.persistence.memory.store;


import com.clubix.api.flows.account.balance.CheckBalanceState;
import com.clubix.api.flows.account.balance.store.CheckBalanceStateStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Profile("local")
@Service
public class InMemoryCheckBalanceStateStore implements CheckBalanceStateStore {

    private static final Duration TTL = Duration.ofMinutes(15);

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public Mono<CheckBalanceState> get(String conversationId) {
        return Mono.fromSupplier(() -> {
            Entry entry = store.get(conversationId);
            if (entry == null || entry.isExpired()) {
                store.remove(conversationId);
                return CheckBalanceState.empty();
            }
            return entry.state;
        });
    }

    @Override
    public Mono<Boolean> put(String conversationId, CheckBalanceState state) {
        return Mono.fromSupplier(() -> {
            store.put(conversationId, new Entry(state));
            return true;
        });
    }

    @Override
    public Mono<Boolean> clear(String conversationId) {
        return Mono.fromSupplier(() -> store.remove(conversationId) != null);
    }

    @Override
    public Mono<Boolean> exists(String conversationId) {
        return Mono.fromSupplier(() -> {
            Entry entry = store.get(conversationId);
            if (entry == null || entry.isExpired()) {
                store.remove(conversationId);
                return false;
            }
            return true;
        });
    }

    // ──────────────
    // TTL wrapper
    // ──────────────
    private static class Entry {
        private final CheckBalanceState state;
        private final Instant createdAt = Instant.now();

        Entry(CheckBalanceState state) {
            this.state = state;
        }

        boolean isExpired() {
            return Instant.now().isAfter(createdAt.plus(TTL));
        }
    }
}