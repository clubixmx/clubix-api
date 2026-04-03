package com.clubix.api.command.formatter;

import com.clubix.usecase.model.response.QueryBalanceResponse;
import org.springframework.stereotype.Component;

@Component
public class QueryBalanceFormatter implements ResponseFormatter<QueryBalanceResponse> {

    @Override
    public String format(QueryBalanceResponse response) {
        if (response.error != null && !response.error.isBlank()) {
            return response.error;
        }
        return String.format("Saldo: $%.2f", response.balance);
    }
}
