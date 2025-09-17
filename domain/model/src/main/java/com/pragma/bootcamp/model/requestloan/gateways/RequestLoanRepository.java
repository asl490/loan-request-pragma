package com.pragma.bootcamp.model.requestloan.gateways;

import com.pragma.bootcamp.common.PageRequest;
import com.pragma.bootcamp.common.PageResponse;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.RequestLoanInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface RequestLoanRepository {

    Flux<RequestLoan> getAll();

    Mono<RequestLoan> findRequestLoanById(Long idRequestLoan);

    Mono<RequestLoan> update(RequestLoan requestLoanRequestLoanUpdate);

    Mono<Void> delete(Long idRequestLoan);

    Mono<RequestLoan> createLoan(RequestLoan requestLoanRequestLoan);

    Mono<PageResponse<RequestLoan>> findWithFilters(PageRequest pageRequest);

    Mono<PageResponse<RequestLoanInfo>> findWithFiltersInfo(PageRequest pageRequest);

    Flux<RequestLoan> findApprovedLoansByDni(String dni);

    Mono<Long> countAllApprovedLoans();

    Mono<BigDecimal> sumAllApprovedLoans();
}
