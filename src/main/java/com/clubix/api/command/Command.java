package com.clubix.api.command;

import com.jmeta.incoming.message.IncomingTextMessage;
import reactor.core.publisher.Mono;

public interface Command {
    Mono<String> process(IncomingTextMessage message);
}
