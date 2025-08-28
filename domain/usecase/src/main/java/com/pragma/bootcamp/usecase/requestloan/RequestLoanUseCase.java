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
        return transactionalGateway.doInTransaction(
                clientRepository.getEmailByDni(requestLoan.getDni())
                        .switchIfEmpty(Mono.error(new ClientNotFoundException(requestLoan.getDni())))
                        .flatMap(email -> loanTypeRepository.findById(requestLoan.getLoanType().getId())
                                .switchIfEmpty(
                                        Mono.error(new LoanTypeNotFoundException("not found")))
                                .filter(loanType -> isAmountInRange(requestLoan.getAmount(), loanType))
                                .switchIfEmpty(Mono.error(new LoanAmountOutOfRangeException()))
                                .flatMap(loanType -> {
                                    requestLoan.setLoanType(loanType);
                                    requestLoan.setEmail(email);
                                    return requestStatusRepository.findById(1L)
                                            .switchIfEmpty(Mono.error(new IllegalStateException(
                                                    "Request status not found: " + PENDING.getDescription())))
                                            .flatMap(requestStatus -> {
                                                requestLoan.setRequestStatus(requestStatus);
                                                return loanRepository.createLoan(requestLoan);
                                            });
                                })));
    }

    private boolean isAmountInRange(BigDecimal amount, LoanType type) {
        System.out.println("Validating amount: " + amount + " for loan type: " + type.getName() +
                " (Min: " + type.getMinAmount() + ", Max: " + type.getMaxAmount() + ")");
        return amount.compareTo(type.getMinAmount()) >= 0 &&
                amount.compareTo(type.getMaxAmount()) <= 0;
    }
}
