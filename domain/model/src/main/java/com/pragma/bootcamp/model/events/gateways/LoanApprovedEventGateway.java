package com.pragma.bootcamp.model.events.gateways;

import com.pragma.bootcamp.model.events.LoanApprovedEvent;
import reactor.core.publisher.Mono;

public interface LoanApprovedEventGateway {
    Mono<Void> sendLoanApprovedEvent(LoanApprovedEvent event);
}
