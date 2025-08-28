package com.pragma.bootcamp.model.loantype.gateways;

import com.pragma.bootcamp.model.loantype.LoanType;

import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<LoanType> findByName(String name);

    Mono<LoanType> findById(Long id);
}
