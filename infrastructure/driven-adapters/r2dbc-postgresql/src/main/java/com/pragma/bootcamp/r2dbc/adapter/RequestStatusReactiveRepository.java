package com.pragma.bootcamp.r2dbc.adapter;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.pragma.bootcamp.r2dbc.entity.RequestStatusEntity;

import reactor.core.publisher.Mono;

public interface RequestStatusReactiveRepository
        extends ReactiveCrudRepository<RequestStatusEntity, Long>, ReactiveQueryByExampleExecutor<RequestStatusEntity> {
    Mono<RequestStatusEntity> findByName(String name);
}
