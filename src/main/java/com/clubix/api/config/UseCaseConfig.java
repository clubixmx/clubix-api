package com.clubix.api.config;

import com.clubix.repository.CustomerPostgreSQLRepository;
import com.clubix.usecase.QueryBalanceUseCase;
import com.usecase.model.request.RequestFactory;
import com.usecase.model.request.RequestFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public RequestFactory requestFactory() {
        return new RequestFactoryImpl("com.clubix.usecase.model.request");
    }

    @Bean
    public QueryBalanceUseCase queryBalanceUseCase(CustomerPostgreSQLRepository customerRepository) {
        return new QueryBalanceUseCase(customerRepository);
    }
}
