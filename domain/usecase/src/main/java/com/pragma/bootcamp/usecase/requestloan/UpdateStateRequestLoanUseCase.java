package com.pragma.bootcamp.usecase.requestloan;

import com.pragma.bootcamp.exception.BusinessException;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requestloan.gateways.NotificationGateway;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import com.pragma.bootcamp.model.requeststatus.Status;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateStateRequestLoanUseCase {

    private final RequestLoanRepository requestLoanRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final NotificationGateway notificationGateway;

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
                            return notificationGateway.sendDecisionNotification(hydratedSavedLoan)
                                                      .thenReturn(hydratedSavedLoan);
                        }
                        // If no notification is needed, just return the hydrated object
                        return Mono.just(hydratedSavedLoan);
                    })
            );
    }
}