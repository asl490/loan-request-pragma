package com.pragma.bootcamp.model.requestloan.gateways;

import com.pragma.bootcamp.common.PageRequest;
import com.pragma.bootcamp.common.PageResponse;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RequestLoanRepository {

    Flux<RequestLoan> getAll();

//    Mono<RequestLoan> update(RequestLoan requestLoanRequestLoanUpdate);

    Mono<Void> delete(Long idRequestLoan);

    Mono<RequestLoan> createLoan(RequestLoan requestLoanRequestLoan);

    Mono<PageResponse<RequestLoan>> findWithFilters(PageRequest pageRequest);
}
