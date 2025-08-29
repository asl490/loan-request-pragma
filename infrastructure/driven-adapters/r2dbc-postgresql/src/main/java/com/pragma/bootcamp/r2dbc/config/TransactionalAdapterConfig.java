package com.pragma.bootcamp.r2dbc.config;

import com.pragma.bootcamp.r2dbc.TransactionAdapter;
import com.pragma.bootcamp.utils.gateways.TransactionalGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
@RequiredArgsConstructor
public class TransactionalAdapterConfig {

    @Bean
    public TransactionalGateway transactionalGateway(TransactionalOperator operator) {
        return new TransactionAdapter(operator);
    }
}