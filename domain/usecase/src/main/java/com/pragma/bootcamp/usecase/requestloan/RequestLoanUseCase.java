package com.pragma.bootcamp.usecase.requestloan;

import static com.pragma.bootcamp.model.requeststatus.Status.PENDING;

import java.math.BigDecimal;

import com.pragma.bootcamp.model.client.exception.ClientNotFoundException;
import com.pragma.bootcamp.model.client.gateways.ClientRepository;
import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.loantype.exception.LoanAmountOutOfRangeException;
import com.pragma.bootcamp.model.loantype.exception.LoanTypeNotFoundException;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requeststatus.exception.RequestStatusNotFoundException;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import com.pragma.bootcamp.utils.gateways.TransactionalGateway;

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

    private Mono<RequestLoan> validateAndBuildRequestLoan(RequestLoan requestLoan, String email) {
        return loanTypeRepository.findById(requestLoan.getLoanType().getId())
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException("Loan type not found")))
                .filter(loanType -> loanType.isAmountInRange(requestLoan.getAmount()))
                .switchIfEmpty(Mono.error(new LoanAmountOutOfRangeException()))
                .flatMap(loanType -> requestStatusRepository.findByName("PENDING")
                        .switchIfEmpty(Mono.error(new RequestStatusNotFoundException("PENDING")))
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
}