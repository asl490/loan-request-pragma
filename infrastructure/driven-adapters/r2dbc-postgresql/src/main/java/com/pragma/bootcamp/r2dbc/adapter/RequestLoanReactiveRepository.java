package com.pragma.bootcamp.r2dbc.adapter;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.pragma.bootcamp.r2dbc.entity.RequestLoanEntity;

public interface RequestLoanReactiveRepository
        extends ReactiveCrudRepository<RequestLoanEntity, Long>, ReactiveQueryByExampleExecutor<RequestLoanEntity> {
    // Mono<RequestLoanEntity> findByName(String name);
}
