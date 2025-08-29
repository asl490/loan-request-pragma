package com.pragma.bootcamp.r2dbc;

import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.pragma.bootcamp.utils.gateways.TransactionalGateway;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TransactionAdapter implements TransactionalGateway {

    private final TransactionalOperator transactionalOperator;

    @Override
    public <T> Mono<T> doInTransaction(Mono<T> operations) {
        return transactionalOperator.transactional(operations);
    }
}