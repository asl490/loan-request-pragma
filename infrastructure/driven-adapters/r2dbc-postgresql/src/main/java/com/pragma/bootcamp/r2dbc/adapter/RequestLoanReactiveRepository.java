package com.pragma.bootcamp.r2dbc.adapter;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.pragma.bootcamp.r2dbc.entity.RequestLoanEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RequestLoanReactiveRepository
        extends ReactiveCrudRepository<RequestLoanEntity, Long>, ReactiveQueryByExampleExecutor<RequestLoanEntity> {
    Flux<RequestLoanEntity> findByDniAndRequestStatus(String dni, Long requestStatus);

    Mono<Long> countByRequestStatus(Long requestStatus);
}
