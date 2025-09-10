package com.pragma.bootcamp.model.requestloan.gateways;

import com.pragma.bootcamp.model.requestloan.RequestLoan;
import reactor.core.publisher.Mono;

public interface NotificationGateway {
    Mono<Void> sendDecisionNotification(RequestLoan requestLoan);
}
