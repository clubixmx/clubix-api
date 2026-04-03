package com.clubix.api.infra.persistence.redis.config;

import com.clubix.api.flows.account.balance.CheckBalanceState;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Profile("aws")
@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, CheckBalanceState> checkBalanceStateRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        Jackson2JsonRedisSerializer<CheckBalanceState> valueSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, CheckBalanceState.class);

        RedisSerializationContext<String, CheckBalanceState> context =
                RedisSerializationContext
                        .<String, CheckBalanceState>newSerializationContext(keySerializer)
                        .value(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}