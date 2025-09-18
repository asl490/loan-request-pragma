package com.pragma.bootcamp.usecase.requestloan;

import com.pragma.bootcamp.common.PageRequest;
import com.pragma.bootcamp.common.PageResponse;
import com.pragma.bootcamp.model.client.Client;
import com.pragma.bootcamp.model.client.exception.ClientNotFoundException;
import com.pragma.bootcamp.model.client.gateways.ClientRepository;
import com.pragma.bootcamp.model.loantype.exception.LoanAmountOutOfRangeException;
import com.pragma.bootcamp.model.loantype.exception.LoanTypeNotFoundException;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import com.pragma.bootcamp.model.message.ActiveLoan;
import com.pragma.bootcamp.model.message.LoanEvaluationMessage;
import com.pragma.bootcamp.model.message.LoanRequestDetails;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.RequestLoanInfo;
import com.pragma.bootcamp.model.requestloan.gateways.LoanEvaluationGateway;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requeststatus.Status;
import com.pragma.bootcamp.model.requeststatus.exception.RequestStatusNotFoundException;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RequestLoanUseCase {

    private final RequestLoanRepository loanRepository;
    private final ClientRepository clientRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final LoanEvaluationGateway loanEvaluationGateway;

    public Mono<RequestLoan> createRequestLoan(RequestLoan requestLoan) {
        return clientRepository.getClientByDni(requestLoan.getDni())
                .switchIfEmpty(Mono.error(new ClientNotFoundException(requestLoan.getDni())))
                .flatMap(client -> {
                    Mono<RequestLoan> builtLoanMono = validateAndBuildRequestLoan(requestLoan, client.getEmail());

                    return builtLoanMono.flatMap(builtLoan ->
                            Mono.zip(Mono.just(builtLoan), loanRepository.createLoan(builtLoan), Mono.just(client))
                    );
                })

                .doOnSuccess(tuple -> {
                    RequestLoan builtLoan = tuple.getT1();
                    RequestLoan savedLoan = tuple.getT2();
                    Client client = tuple.getT3();

                    savedLoan.setLoanType(builtLoan.getLoanType());

                    if (Boolean.TRUE.equals(builtLoan.getLoanType().getValidationAutomatic())) {
                        buildAndSendMessage(savedLoan, client).subscribe();
                    }
                })
                .map(Tuple2::getT2);
    }

    private Mono<Void> buildAndSendMessage(RequestLoan createdLoan, Client client) {
        return loanRepository.findApprovedLoansByDni(client.getDocument())
                .flatMap(this::enrichLoanWithFullLoanType)
                .collectList()
                .flatMap(activeLoans -> {
                    LoanEvaluationMessage message = toLoanEvaluationMessage(createdLoan, client, activeLoans);
                    return loanEvaluationGateway.sendForEvaluation(message);
                });
    }

    private Mono<RequestLoan> enrichLoanWithFullLoanType(RequestLoan loan) {
        return loanTypeRepository.findById(loan.getLoanType().getId())
                .map(fullLoanType -> {
                    loan.setLoanType(fullLoanType);
                    return loan;
                })
                .defaultIfEmpty(loan);
    }

    private LoanEvaluationMessage toLoanEvaluationMessage(RequestLoan createdLoan, Client client, List<RequestLoan> activeLoans) {
        LoanRequestDetails loanRequestDetails = LoanRequestDetails.builder()
                .id(createdLoan.getId())
                .amount(createdLoan.getAmount())
                .dni(createdLoan.getDni())
                .termMonths(createdLoan.getTerm())
                .loanTypeId(createdLoan.getLoanType().getId())
                .email(createdLoan.getEmail())
                .annualInterestRate(createdLoan.getLoanType().getInterestRate())
                .build();

        List<ActiveLoan> activeLoanDetails = activeLoans.stream()
                .map(loan -> ActiveLoan.builder()
                        .amount(loan.getAmount())
                        .annualInterestRate(loan.getLoanType().getInterestRate())
                        .termMonths(loan.getTerm())
                        .build())
                .collect(Collectors.toList());

        return LoanEvaluationMessage.builder()
                .loanRequest(loanRequestDetails)
                .baseSalary(client.getSalary())
                .activeLoans(activeLoanDetails)
                .build();
    }

    public Mono<RequestLoan> getById(Long id) {
        return loanRepository.findRequestLoanById(id);
    }

    public Flux<RequestLoan> findApproveLoansByDni(String dni) {
        return loanRepository.findApprovedLoansByDni(dni);
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
