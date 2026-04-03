package com.clubix.api.flows.core;

import reactor.core.publisher.Mono;

import java.util.Locale;

public abstract class ReactiveConfirmableFlow<S> implements ReactiveToolFlow {

    // ---- hooks (reactivos donde aplica) ----
    public abstract boolean hasIntent(String userText);

    protected abstract S emptyState();

    protected abstract Mono<S> load(String conversationId);
    protected abstract Mono<Void> save(String conversationId, S state);
    protected abstract Mono<Void> clear(String conversationId);
    protected abstract Mono<Boolean> exists(String conversationId);

    protected abstract S applyCorrections(S state, String userText);

    protected abstract boolean isComplete(S state);

    protected abstract boolean isAwaitingConfirmation(S state);
    protected abstract S setAwaitingConfirmation(S state, boolean value);

    protected abstract String askNext(S state);
    protected abstract String confirmPrompt(S state);

    protected abstract Mono<String> execute(S state);

    // ---- yes/no helpers ----
    protected boolean isYes(String text) {
        String t = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        return t.equals("si") || t.equals("sí") || t.equals("s") || t.equals("yes")
                || t.equals("ok") || t.equals("va") || t.equals("confirmo") || t.equals("adelante");
    }

    protected boolean isNo(String text) {
        String t = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        return t.equals("no") || t.equals("n") || t.equals("cancelar") || t.equals("cancela")
                || t.equals("negativo");
    }

    protected boolean isPureYes(String text) {
        String trimmed = text == null ? "" : text.trim();
        return isYes(trimmed) && trimmed.split("\\s+").length == 1;
    }

    protected boolean isPureNo(String text) {
        String trimmed = text == null ? "" : text.trim();
        return isNo(trimmed) && trimmed.split("\\s+").length == 1;
    }

    @Override
    public Mono<Boolean> isActive(String conversationId) {
        return exists(conversationId);
    }

    // ---- handle reactivo ----
    public Mono<String> handle(String conversationId, String userText) {
        final String text = userText == null ? "" : userText.trim();

        return exists(conversationId)
                .flatMap(active -> active ? load(conversationId) : Mono.just(emptyState()))
                .flatMap(state -> {

                    // A) esperando confirmación
                    if (isAwaitingConfirmation(state)) {
                        boolean yes = isPureYes(text);
                        boolean no  = isPureNo(text);

                        S corrected = applyCorrections(state, text);
                        boolean changed = !corrected.equals(state);

                        if (changed && !yes && !no) {
                            S awaiting = setAwaitingConfirmation(corrected, true);
                            return save(conversationId, awaiting)
                                    .thenReturn(isComplete(awaiting) ? confirmPrompt(awaiting) : askNext(awaiting));
                        }

                        if (yes) {
                            return execute(state)
                                    .flatMap(result -> clear(conversationId).thenReturn(result));
                        }

                        if (no) {
                            return clear(conversationId).thenReturn("Operación cancelada.");
                        }

                        return Mono.just("Necesito confirmación explícita. Responde: sí o no. (O envía una corrección).");
                    }

                    // B) no esperando confirmación: intentar completar
                    S updated = applyCorrections(state, text);

                    // guardar
                    Mono<Void> persist = save(conversationId, updated);

                    // C) si ya completo: pedir confirmación (sin ejecutar)
                    if (isComplete(updated)) {
                        S awaiting = setAwaitingConfirmation(updated, true);
                        return save(conversationId, awaiting).thenReturn(confirmPrompt(awaiting));
                    }

                    // D) preguntar lo siguiente
                    return persist.thenReturn(askNext(updated));
                });
    }
}