package com.pragma.bootcamp.sqs.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.bootcamp.model.events.LoanApprovedEvent;
import com.pragma.bootcamp.model.events.gateways.LoanApprovedEventGateway;
import com.pragma.bootcamp.model.message.LoanEvaluationMessage;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.LoanEvaluationGateway;
import com.pragma.bootcamp.model.requestloan.gateways.NotificationGateway;
import com.pragma.bootcamp.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationGateway, LoanEvaluationGateway, LoanApprovedEventGateway {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> sendDecisionNotification(RequestLoan requestLoan) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(requestLoan))
            .flatMap(messageBody -> {
                log.info("Enviando mensaje a SQS para solicitud ID: {}", requestLoan.getId());
                SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(properties.queueUrlNotification())
                    .messageBody(messageBody)
                    .build();
                
                return Mono.fromFuture(client.sendMessage(sendMsgRequest));
            })
            .doOnSuccess(response -> log.info("Mensaje enviado a SQS con éxito. MessageId: {}", response.messageId()))
            .doOnError(e -> log.error("Error al enviar mensaje a SQS", e))
            .then();
    }

    @Override
    public Mono<Void> sendForEvaluation(LoanEvaluationMessage message) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(message))
                .flatMap(messageBody -> {
                    log.info("Enviando mensaje a SQS para validacion: {}", messageBody);
                    SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                            .queueUrl(properties.queueUrlDebtCapacity())
                            .messageBody(messageBody)
                            .build();

                    return Mono.fromFuture(client.sendMessage(sendMsgRequest));
                })
                .doOnSuccess(response -> log.info("Mensaje enviado a SQS con éxito. MessageId: {}", response.messageId()))
                .doOnError(e -> log.error("Error al enviar mensaje a SQS", e))
                .then();
    }

    @Override
    public Mono<Void> sendLoanApprovedEvent(LoanApprovedEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(messageBody -> {
                    log.info("Enviando mensaje a SQS para validacion: {}", messageBody);
                    SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                            .queueUrl(properties.queueUrlLoanApproved())
                            .messageBody(messageBody)
                            .build();

                    return Mono.fromFuture(client.sendMessage(sendMsgRequest));
                })
                .doOnSuccess(response -> log.info("Mensaje enviado a SQS con éxito. MessageId: {}", response.messageId()))
                .doOnError(e -> log.error("Error al enviar mensaje a SQS", e))
                .then();
    }
}
