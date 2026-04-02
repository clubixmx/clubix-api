package com.clubix.api.incoming;

import com.jmeta.incoming.message.IncomingMessage;
import com.jmeta.incoming.message.IncomingTextMessage;
import com.jmeta.incoming.processor.MessageProcessor;
import com.jmeta.outgoing.MarkAsReadSender;
import com.jmeta.outgoing.MessageSender;
import com.jmeta.outgoing.TypingIndicatorSender;
import com.jmeta.outgoing.message.TextContent;
import com.jmeta.outgoing.message.WhatsappMessage;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class TextMessageProcessor implements MessageProcessor {
    private final MessageSender messageSender;
    private final MarkAsReadSender markAsReadSender;
    private final TypingIndicatorSender typingIndicatorSender;

    @Override
    public void process(IncomingMessage incomingMessage) {
        IncomingTextMessage incomingTextMessage = (IncomingTextMessage) incomingMessage;
        String messageId = incomingTextMessage.message().id();

        markAsReadSender.markAsRead(messageId)
                .then(Mono.defer(() -> typingIndicatorSender.send(messageId)))
                .then(Mono.defer(() -> messageSender.send(WhatsappMessage.builder()
                        .to(incomingMessage.profile().waId())
                        .type("text")
                        .text(TextContent.builder()
                                .body(String.format("Echo: %s", incomingTextMessage.text()))
                                .build())
                        .build())))
                .subscribe(
                        null,
                        error -> log.error("Error processing message {}: {}", messageId, error.getMessage())
                );
    }
}
