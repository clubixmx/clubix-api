package com.clubix.api.config;

import com.clubix.api.command.Command;
import com.clubix.api.command.QueryBalanceCommand;
import com.clubix.api.incoming.TextMessageProcessor;
import com.jmeta.incoming.processor.MessageProcessor;
import com.jmeta.outgoing.MarkAsReadSender;
import com.jmeta.outgoing.MessageSender;
import com.jmeta.outgoing.TypingIndicatorSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ProcessorConfig {

    private final MessageSender messageSender;
    private final MarkAsReadSender markAsReadSender;
    private final TypingIndicatorSender typingIndicatorSender;
    private final QueryBalanceCommand queryBalanceCommand;

    @Bean
    public Map<String, Command> commandRegistry() {
        return Map.of(
                "saldo", queryBalanceCommand
        );
    }

    @Bean
    public Map<String, MessageProcessor> messageProcessorMap(Map<String, Command> commandRegistry) {
        return Map.of(
                "text", new TextMessageProcessor(messageSender, markAsReadSender, typingIndicatorSender, commandRegistry)
        );
    }
}
