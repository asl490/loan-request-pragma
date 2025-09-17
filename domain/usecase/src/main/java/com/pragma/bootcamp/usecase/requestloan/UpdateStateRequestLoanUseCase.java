package com.pragma.bootcamp.usecase.requestloan;

import com.pragma.bootcamp.exception.BusinessException;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requestloan.gateways.NotificationGateway;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import com.pragma.bootcamp.model.requeststatus.Status;
import com.pragma.bootcamp.model.events.LoanApprovedEvent; // NEW IMPORT
import com.pragma.bootcamp.model.events.gateways.LoanApprovedEventGateway; // NEW IMPORT
import java.math.BigDecimal; // NEW IMPORT
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateStateRequestLoanUseCase {

    private final RequestLoanRepository requestLoanRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final NotificationGateway notificationGateway;
    private final LoanApprovedEventGateway loanApprovedEventGateway; // CHANGED FIELD

    public Mono<RequestLoan> updateState(Long requestId, Status newStatusEnum) {

        return requestStatusRepository.findByName(newStatusEnum.name())
            .switchIfEmpty(Mono.error(new BusinessException(BusinessException.Type.REQUEST_LOAN_NOT_FOUND)))
            .flatMap(correctRequestStatus ->
                requestLoanRepository.findRequestLoanById(requestId)
                    .switchIfEmpty(Mono.error(new BusinessException(BusinessException.Type.REQUEST_LOAN_NOT_FOUND)))
                    .flatMap(requestLoan -> {
                        RequestLoan updatedLoan = requestLoan.toBuilder()
                                .requestStatus(correctRequestStatus)
                                .build();
                        return requestLoanRepository.update(updatedLoan);
                    })
                    .map(incompleteSavedLoan -> incompleteSavedLoan.toBuilder()
                            .requestStatus(correctRequestStatus)
                            .build()
                    )
                    .flatMap(hydratedSavedLoan -> {
                        // Conditionally send the notification using the hydrated object
                        if (newStatusEnum == Status.APPROVED || newStatusEnum == Status.REJECTED) {
                            Mono<RequestLoan> notificationMono = notificationGateway.sendDecisionNotification(hydratedSavedLoan)
                                                      .thenReturn(hydratedSavedLoan);

                            if (newStatusEnum == Status.APPROVED) {
                                return sendLoanApprovedSqsEvent(hydratedSavedLoan, notificationMono);
                            }
                            return notificationMono;
                        }
                        // If no notification is needed, just return the hydrated object
                        return Mono.just(hydratedSavedLoan);
                    })
            );
    }

    private Mono<RequestLoan> sendLoanApprovedSqsEvent(RequestLoan hydratedSavedLoan, Mono<RequestLoan> notificationMono) {
        return Mono.zip(
            requestLoanRepository.countAllApprovedLoans(),
            requestLoanRepository.sumAllApprovedLoans()
        )
        .flatMap(tuple -> {
            Long newTotalCount = tuple.getT1();
            BigDecimal newTotalAmount = tuple.getT2();

            LoanApprovedEvent event = LoanApprovedEvent.builder()
                    .approvedAmount(hydratedSavedLoan.getAmount().toPlainString())
                    .newTotalCount(newTotalCount.intValue())
                    .newTotalAmount(newTotalAmount.toPlainString())
                    .build();
            return loanApprovedEventGateway.sendLoanApprovedEvent(event)
                    .then(notificationMono);
        });
    }
}