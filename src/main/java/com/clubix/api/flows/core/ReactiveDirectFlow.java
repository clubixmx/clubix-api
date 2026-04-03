package com.clubix.api.flows.core;

import reactor.core.publisher.Mono;

/**
 * Base para flows que NO requieren confirmación del usuario.
 * Una vez que el estado tiene todos los elementos necesarios ({@link #isComplete}),
 * ejecuta el caso de uso directamente y limpia el estado.
 *
 * <p>Flujo de {@link #handle}:
 * <ol>
 *   <li>Carga el estado actual (o vacío si no hay conversación activa).</li>
 *   <li>Aplica correcciones con el texto del usuario.</li>
 *   <li>Si completo → {@link #execute} + {@link #clear}.</li>
 *   <li>Si incompleto → persiste y devuelve {@link #askNext}.</li>
 * </ol>
 */
public abstract class ReactiveDirectFlow<S> implements ReactiveToolFlow {

    // ── Hooks ──────────────────────────────────────────────────────────────────

    protected abstract S emptyState();

    protected abstract Mono<S> load(String conversationId);
    protected abstract Mono<Void> save(String conversationId, S state);
    protected abstract Mono<Void> clear(String conversationId);
    protected abstract Mono<Boolean> exists(String conversationId);

    protected abstract S applyCorrections(S state, String userText);

    protected abstract boolean isComplete(S state);

    protected abstract String askNext(S state);

    protected abstract Mono<String> execute(S state);

    // ── ReactiveToolFlow ───────────────────────────────────────────────────────

    @Override
    public Mono<Boolean> isActive(String conversationId) {
        return exists(conversationId);
    }

    @Override
    public Mono<String> handle(String conversationId, String userText) {
        final String text = userText == null ? "" : userText.trim();

        return exists(conversationId)
                .flatMap(active -> active ? load(conversationId) : Mono.just(emptyState()))
                .flatMap(state -> {
                    S updated = applyCorrections(state, text);

                    if (isComplete(updated)) {
                        return execute(updated)
                                .flatMap(result -> clear(conversationId).thenReturn(result));
                    }

                    return save(conversationId, updated)
                            .thenReturn(askNext(updated));
                });
    }
}

