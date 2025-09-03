package com.pragma.bootcamp.r2dbc.clientconection;

import com.pragma.bootcamp.model.client.Client;
import com.pragma.bootcamp.model.client.gateways.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class ClientGatewayImpl implements ClientRepository {

    private final WebClient clientWebClient;
    private final ClientProperties clientProperties;

    @Override
    public Mono<String> getEmailByDni(String dni) {
        return clientWebClient
                .get()
                .uri(clientProperties.getUserPath(), dni)
                .retrieve()
                .bodyToMono(Client.class)
                .map(Client::getEmail);
    }
}