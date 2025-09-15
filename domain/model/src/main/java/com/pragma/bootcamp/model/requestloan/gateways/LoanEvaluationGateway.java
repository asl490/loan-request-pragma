package com.pragma.bootcamp.model.requestloan.gateways;

import com.pragma.bootcamp.model.message.LoanEvaluationMessage;
import reactor.core.publisher.Mono;

public interface LoanEvaluationGateway {
    Mono<Void> sendForEvaluation(LoanEvaluationMessage message);
}
