package com.pragma.bootcamp.sqs.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.NotificationGateway;
import com.pragma.bootcamp.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationGateway {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> sendDecisionNotification(RequestLoan requestLoan) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(requestLoan))
            .flatMap(messageBody -> {
                log.info("Enviando mensaje a SQS para solicitud ID: {}", requestLoan.getId());
                SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(properties.queueUrl())
                    .messageBody(messageBody)
                    .build();
                
                return Mono.fromFuture(client.sendMessage(sendMsgRequest));
            })
            .doOnSuccess(response -> log.info("Mensaje enviado a SQS con éxito. MessageId: {}", response.messageId()))
            .doOnError(e -> log.error("Error al enviar mensaje a SQS", e))
            .then();
    }
}
