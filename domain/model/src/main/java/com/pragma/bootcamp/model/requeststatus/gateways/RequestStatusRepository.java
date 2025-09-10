package com.pragma.bootcamp.model.requeststatus.gateways;

import com.pragma.bootcamp.model.requeststatus.RequestStatus;
import reactor.core.publisher.Mono;

public interface RequestStatusRepository {
    Mono<RequestStatus> findByName(String status);

    Mono<RequestStatus> findById(Long idRequestStatus);

}
