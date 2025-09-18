package com.pragma.bootcamp.usecase.requestloan;

import com.pragma.bootcamp.exception.BusinessException;
import com.pragma.bootcamp.model.events.LoanApprovedEvent;
import com.pragma.bootcamp.model.events.gateways.LoanApprovedEventGateway;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.NotificationGateway;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requeststatus.RequestStatus;
import com.pragma.bootcamp.model.requeststatus.Status;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateStateRequestLoanUseCase {

    private final RequestLoanRepository requestLoanRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final NotificationGateway notificationGateway;
    private final LoanApprovedEventGateway loanApprovedEventGateway;

    public Mono<RequestLoan> updateState(Long requestId, Status newStatusEnum) {
        return getStatus(newStatusEnum)
                .zipWith(requestLoanRepository.findRequestLoanById(requestId)
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessException.Type.REQUEST_LOAN_NOT_FOUND))))
                .flatMap(tuple -> {
                    var status = tuple.getT1();
                    var requestLoan = tuple.getT2().toBuilder().requestStatus(status).build();

                    return requestLoanRepository.update(requestLoan)
                            .flatMap(updatedLoan -> handlePostUpdateActions(updatedLoan, newStatusEnum));
                });
    }

    private Mono<RequestStatus> getStatus(Status statusEnum) {
        return requestStatusRepository.findByName(statusEnum.name())
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.Type.REQUEST_LOAN_NOT_FOUND)));
    }

    private Mono<RequestLoan> handlePostUpdateActions(RequestLoan loan, Status status) {
        if (status == Status.APPROVED) {
            return sendLoanApprovedEvent(loan)
                    .then(sendNotification(loan));
        } else if (status == Status.REJECTED) {
            return sendNotification(loan);
        }
        return Mono.just(loan);
    }

    private Mono<RequestLoan> sendNotification(RequestLoan loan) {
        return notificationGateway.sendDecisionNotification(loan).thenReturn(loan);
    }

    private Mono<Void> sendLoanApprovedEvent(RequestLoan loan) {
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .approvedAmount(loan.getAmount().toPlainString())
                .build();
        return loanApprovedEventGateway.sendLoanApprovedEvent(event);
    }
}
