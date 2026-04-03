package com.clubix.api.flows.account.balance;

import com.clubix.api.flows.account.balance.store.CheckBalanceStateStore;
import com.clubix.api.flows.core.ReactiveConfirmableFlow;
import com.clubix.api.tools.AccountTools;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Order(10)
@Service
public class CheckBalanceFlow extends ReactiveConfirmableFlow<CheckBalanceState> {

    private final CheckBalanceStateStore store;
    private final CheckBalanceParser parser;
    private final AccountTools accountTools;

    public CheckBalanceFlow(CheckBalanceStateStore store,
                            CheckBalanceParser parser,
                            AccountTools accountTools) {
        this.store = store;
        this.parser = parser;
        this.accountTools = accountTools;
    }

    @Override public String name() { return "CONSULTAR_SALDO"; }

    @Override
    public boolean hasIntent(String userText) {
        String text = userText == null ? "" : userText.trim().toLowerCase(Locale.ROOT);
        return text.startsWith("consultar")
                || text.startsWith("saldo")
                || text.contains("consultar saldo");
    }

    @Override protected CheckBalanceState emptyState() {
        return CheckBalanceState.empty();
    }

    @Override
    protected Mono<CheckBalanceState> load(String conversationId) {
        return store.get(conversationId);
    }

    @Override
    protected Mono<Void> save(String conversationId, CheckBalanceState state) {
        return store.put(conversationId, state).then();
    }

    @Override
    protected Mono<Void> clear(String conversationId) {
        return store.clear(conversationId).then();
    }

    @Override
    protected Mono<Boolean> exists(String conversationId) {
        return store.exists(conversationId);
    }

    @Override
    protected CheckBalanceState applyCorrections(CheckBalanceState s, String userText) {
        var targetOpt = parser.parseTarget(userText);
        if (targetOpt.isEmpty()) return s;

        String newTarget = targetOpt.get();
        if (newTarget.equals(s.target())) return s;

        return s.withTarget(newTarget);
    }

    @Override
    protected boolean isComplete(CheckBalanceState s) {
        return s.complete();
    }

    @Override
    protected boolean isAwaitingConfirmation(CheckBalanceState s) {
        return s.awaitingConfirmation();
    }

    @Override
    protected CheckBalanceState setAwaitingConfirmation(CheckBalanceState s, boolean value) {
        return s.awaitingConfirmation(value);
    }

    @Override
    protected String askNext(CheckBalanceState s) {
        return "¿De qué cuenta (10 dígitos) deseas consultar el saldo? Ejemplo: 5511566012";
    }

    @Override
    protected String confirmPrompt(CheckBalanceState s) {
        return "Confirmas consultar el saldo de la cuenta " + s.target() + "? Responde: sí o no.";
    }

    @Override
    protected Mono<String> execute(CheckBalanceState s) {
        return accountTools.checkBalance(s.target());
    }
}