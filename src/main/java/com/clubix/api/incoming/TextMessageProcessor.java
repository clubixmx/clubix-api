package com.clubix.api.incoming;

import com.clubix.api.command.Command;
import com.jmeta.incoming.message.IncomingMessage;
import com.jmeta.incoming.message.IncomingTextMessage;
import com.jmeta.incoming.processor.MessageProcessor;
import com.jmeta.outgoing.MarkAsReadSender;
import com.jmeta.outgoing.MessageSender;
import com.jmeta.outgoing.TypingIndicatorSender;
import com.jmeta.outgoing.message.TextContent;
import com.jmeta.outgoing.message.WhatsappMessage;
import com.usecase.shared.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class TextMessageProcessor implements MessageProcessor {
    private final MessageSender messageSender;
    private final MarkAsReadSender markAsReadSender;
    private final TypingIndicatorSender typingIndicatorSender;
    private final Map<String, Command> commandRegistry;

    @Override
    public void process(IncomingMessage incomingMessage) {
        IncomingTextMessage incomingTextMessage = (IncomingTextMessage) incomingMessage;
        String messageId = incomingTextMessage.message().id();

        markAsReadSender.markAsRead(messageId)
                .then(Mono.defer(() -> typingIndicatorSender.send(messageId)))
                .then(Mono.defer(() -> dispatch(incomingTextMessage)))
                .flatMap(responseText -> messageSender.send(WhatsappMessage.builder()
                        .to(incomingMessage.profile().waId())
                        .type("text")
                        .text(TextContent.builder()
                                .body(responseText)
                                .build())
                        .build()))
                .subscribe(
                        null,
                        error -> log.error("Error processing message {}: {}", messageId, error.getMessage())
                );
    }

    private Mono<String> dispatch(IncomingTextMessage message) {
        String token = message.text().split(" ")[0].toLowerCase();
        return Optional.ofNullable(commandRegistry.get(token))
                .map(command -> command.process(message)
                        .onErrorResume(ValidationException.class, e -> Mono.just(e.getMessage()))
                        .onErrorResume(e -> Mono.just("Service unavailable")))
                .orElseGet(() -> Mono.just(String.format("Echo: %s", message.text())));
    }
}
