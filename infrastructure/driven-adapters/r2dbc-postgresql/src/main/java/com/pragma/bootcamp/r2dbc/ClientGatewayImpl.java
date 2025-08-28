package com.pragma.bootcamp.r2dbc;

import org.springframework.stereotype.Component;

import com.pragma.bootcamp.model.client.gateways.ClientRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ClientGatewayImpl implements ClientRepository {

        private final WebClient clientWebClient;

        @Override
        public Mono<String> getEmailByDni(String dni) {
                return clientWebClient
                                .get()
                                .uri("/api/v1/usuarios/{dni}", dni)
                                .retrieve()
                                .bodyToMono(ClientResponse.class)
                                .map(ClientResponse::getEmail);
        }
}