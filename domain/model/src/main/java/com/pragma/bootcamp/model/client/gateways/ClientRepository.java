package com.pragma.bootcamp.model.client.gateways;

import com.pragma.bootcamp.model.client.Client;
import reactor.core.publisher.Mono;

public interface ClientRepository {

    Mono<String> getEmailByDni(String dni);

    Mono<Client>  getClientByDni(String dni);

}
