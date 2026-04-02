package com.clubix.api.config;

import com.clubix.repository.CustomerRepository;
import com.clubix.usecase.QueryBalanceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public QueryBalanceUseCase queryBalanceUseCase(CustomerRepository customerRepository) {
        return new QueryBalanceUseCase(customerRepository);
    }
}

