package com.pragma.bootcamp.model.client.gateways;

import reactor.core.publisher.Mono;

public interface ClientRepository {

    Mono<String> getEmailByDni(String dni);

}
