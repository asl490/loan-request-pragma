package com.pragma.bootcamp.r2dbc.adapter;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import com.pragma.bootcamp.model.requeststatus.RequestStatus;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import com.pragma.bootcamp.r2dbc.entity.RequestStatusEntity;
import com.pragma.bootcamp.r2dbc.helper.ReactiveAdapterOperations;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class RequestStatusReactiveRepositoryAdapter extends
        ReactiveAdapterOperations<RequestStatus, RequestStatusEntity, Long, RequestStatusReactiveRepository>
        implements RequestStatusRepository {
    public RequestStatusReactiveRepositoryAdapter(RequestStatusReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, RequestStatus.class));
    }

    @Override
    public Mono<RequestStatus> findByName(String name) {
        return repository.findByName(name)
                .map(this::toEntity);
    }
}
