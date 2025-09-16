package com.pragma.bootcamp.sqs.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.bootcamp.model.message.LoanApproved;
import com.pragma.bootcamp.model.requeststatus.Status;
import com.pragma.bootcamp.usecase.requestloan.UpdateStateRequestLoanUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.IOException;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final ObjectMapper objectMapper;
    private final UpdateStateRequestLoanUseCase updateStateRequestLoanUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        try {
            LoanApproved loanApproved = objectMapper.readValue(message.body(), LoanApproved.class);
            Status status = Status.valueOf(loanApproved.getStatus());
            return updateStateRequestLoanUseCase.updateState(loanApproved.getRequestId(), status).then();
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
