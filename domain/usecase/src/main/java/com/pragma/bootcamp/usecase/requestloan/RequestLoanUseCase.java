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
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import com.pragma.bootcamp.utils.gateways.TransactionalGateway;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RequestLoanUseCase {

    private final RequestLoanRepository loanRepository;
    private final TransactionalGateway transactionalGateway;
    private final ClientRepository clientRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final RequestStatusRepository requestStatusRepository;

    public Mono<RequestLoan> createRequestLoan(RequestLoan requestLoan) {
        return transactionalGateway.executeInTransaction(
                clientRepository.getEmailByDni(requestLoan.getDni())
                        .switchIfEmpty(Mono.error(new ClientNotFoundException(requestLoan.getDni())))
                        .flatMap(email -> loanTypeRepository.findByName(requestLoan.getLoanType().getName())
                                .switchIfEmpty(
                                        Mono.error(new LoanTypeNotFoundException(requestLoan.getLoanType().getName())))
                                .filter(loanType -> isAmountInRange(requestLoan.getAmount(), loanType))
                                .switchIfEmpty(Mono.error(new LoanAmountOutOfRangeException()))
                                .flatMap(loanType -> {
                                    requestLoan.setLoanType(loanType);
                                    requestLoan.setEmail(email);
                                    return requestStatusRepository.findByName(PENDING.getDescription())
                                            .switchIfEmpty(Mono.error(new IllegalStateException(
                                                    "Request status not found: " + PENDING.getDescription())))
                                            .flatMap(requestStatus -> {
                                                requestLoan.setRequestStatus(requestStatus);
                                                return loanRepository.create(requestLoan);
                                            });
                                })));
    }

    private boolean isAmountInRange(BigDecimal amount, LoanType type) {
        return amount.compareTo(type.getMinimumAmount()) >= 0 &&
                amount.compareTo(type.getMaximumAmount()) <= 0;
    }
}
