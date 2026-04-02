package com.clubix.api.command;

import com.clubix.api.command.formatter.ResponseFormatter;
import com.clubix.usecase.model.response.QueryBalanceResponse;
import com.jmeta.incoming.message.IncomingTextMessage;
import com.usecase.UseCase;
import com.usecase.model.request.RequestFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class QueryBalanceCommand implements Command {

    private final UseCase queryBalanceUseCase;
    private final RequestFactory requestFactory;
    private final ResponseFormatter<QueryBalanceResponse> formatter;

    public QueryBalanceCommand(@Qualifier("queryBalanceUseCase") UseCase queryBalanceUseCase,
                               RequestFactory requestFactory,
                               ResponseFormatter<QueryBalanceResponse> formatter) {
        this.queryBalanceUseCase = queryBalanceUseCase;
        this.requestFactory = requestFactory;
        this.formatter = formatter;
    }

    @Override
    public Mono<String> process(IncomingTextMessage message) {
        String customerId = message.text().split(" ")[1];

        var request = requestFactory.get("QueryBalanceRequest", Map.of("customerId", customerId));

        return queryBalanceUseCase.execute(request)
                .cast(QueryBalanceResponse.class)
                .map(formatter::format);
    }
}
