package com.clubix.api.tools;

import com.clubix.api.command.formatter.ResponseFormatter;
import com.clubix.usecase.model.response.QueryBalanceResponse;
import com.usecase.UseCase;
import com.usecase.model.request.RequestFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class AccountTools {

    private final UseCase queryBalanceUseCase;
    private final RequestFactory requestFactory;
    private final ResponseFormatter<QueryBalanceResponse> formatter;

    public AccountTools(@Qualifier("queryBalanceUseCase") UseCase queryBalanceUseCase,
                        RequestFactory requestFactory,
                        ResponseFormatter<QueryBalanceResponse> formatter) {
        this.queryBalanceUseCase = queryBalanceUseCase;
        this.requestFactory = requestFactory;
        this.formatter = formatter;
    }

    @Tool(name = "consultarSaldo", description = "Consultar saldo de una cuenta objetivo")
    public Mono<String> checkBalance(
            @ToolParam(description = "Representa la cuenta destino (10 dígitos)") String customerId
    ) {
        var request = requestFactory.get("QueryBalanceRequest", Map.of("customerId", customerId));

        return queryBalanceUseCase.execute(request)
                .cast(QueryBalanceResponse.class)
                .map(formatter::format);
    }
}
