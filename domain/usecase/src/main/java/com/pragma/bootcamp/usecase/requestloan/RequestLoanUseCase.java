package com.pragma.bootcamp.usecase.requestloan;

import com.pragma.bootcamp.common.PageRequest;
import com.pragma.bootcamp.common.PageResponse;
import com.pragma.bootcamp.model.client.exception.ClientNotFoundException;
import com.pragma.bootcamp.model.client.gateways.ClientRepository;
import com.pragma.bootcamp.model.loantype.exception.LoanAmountOutOfRangeException;
import com.pragma.bootcamp.model.loantype.exception.LoanTypeNotFoundException;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.RequestLoanInfo;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requeststatus.Status;
import com.pragma.bootcamp.model.requeststatus.exception.RequestStatusNotFoundException;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RequestLoanUseCase {

    private final RequestLoanRepository loanRepository;
    private final ClientRepository clientRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final RequestStatusRepository requestStatusRepository;

    public Mono<RequestLoan> createRequestLoan(RequestLoan requestLoan) {

        return clientRepository.getEmailByDni(requestLoan.getDni())
                .switchIfEmpty(Mono.error(new ClientNotFoundException(requestLoan.getDni())))
                .flatMap(email -> validateAndBuildRequestLoan(requestLoan, email))
                .flatMap(loanRepository::createLoan);
    }

    public Mono<RequestLoan> getById(Long id) {
        return loanRepository.findRequestLoanById(id);
    }

    private Mono<RequestLoan> validateAndBuildRequestLoan(RequestLoan requestLoan, String email) {
        return loanTypeRepository.findById(requestLoan.getLoanType().getId())
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException("Loan type not found")))
                .filter(loanType -> loanType.isAmountInRange(requestLoan.getAmount()))
                .switchIfEmpty(Mono.error(new LoanAmountOutOfRangeException()))
                .flatMap(loanType -> requestStatusRepository.findByName(Status.PENDING.name())
                        .switchIfEmpty(Mono.error(new RequestStatusNotFoundException(Status.PENDING.name())))
                        .map(requestStatus -> RequestLoan.builder()
                                .dni(requestLoan.getDni())
                                .amount(requestLoan.getAmount())
                                .term(requestLoan.getTerm())
                                .loanType(loanType)
                                .email(email)
                                .requestStatus(requestStatus)
                                .build())
                );
    }

    public Mono<PageResponse<RequestLoan>> execute(PageRequest pageRequest) {
        return loanRepository.findWithFilters(pageRequest);

    }

    public Mono<PageResponse<RequestLoanInfo>> executeInfo(PageRequest pageRequest) {
        return loanRepository.findWithFiltersInfo(pageRequest);

    }

}