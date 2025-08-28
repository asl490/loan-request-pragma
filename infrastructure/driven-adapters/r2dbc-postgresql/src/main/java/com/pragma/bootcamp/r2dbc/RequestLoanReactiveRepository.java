package com.pragma.bootcamp.r2dbc;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.pragma.bootcamp.r2dbc.entity.RequestLoanEntity;

// TODO: This file is just an example, you should delete or modify it
public interface RequestLoanReactiveRepository
        extends ReactiveCrudRepository<RequestLoanEntity, Long>, ReactiveQueryByExampleExecutor<RequestLoanEntity> {

}
