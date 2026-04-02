package com.clubix.api.command;

import com.clubix.usecase.model.response.QueryBalanceResponse;
import com.jmeta.incoming.message.IncomingTextMessage;
import com.jmeta.incoming.message.Message;
import com.jmeta.incoming.message.Profile;
import com.usecase.UseCase;
import com.usecase.model.request.RequestFactory;
import com.usecase.model.request.RequestFactoryImpl;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryBalanceCommandTest {

    private static final RequestFactory requestFactory =
            new RequestFactoryImpl("com.clubix.usecase.model.request");

    @Test
    void should_return_formatted_balance_message() {
        IncomingTextMessage message = new IncomingTextMessage(
                new Profile("Test User", "5491112345678"),
                new Message("5491112345678", UUID.randomUUID().toString()),
                "text",
                "saldo 5511566012"
        );

        UseCase useCase = mock(UseCase.class);
        when(useCase.execute(any())).thenReturn(
                Mono.just(QueryBalanceResponse.builder()
                        .status("SUCCESS")
                        .balance(100.00)
                        .build())
        );

        Command command = new QueryBalanceCommand(useCase, requestFactory);

        StepVerifier.create(command.process(message))
                .expectNext("Saldo: $100.00")
                .verifyComplete();
    }
}
