package com.pragma.bootcamp.r2dbc.adapter;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import com.pragma.bootcamp.r2dbc.entity.LoanTypeEntity;
import com.pragma.bootcamp.r2dbc.helper.ReactiveAdapterOperations;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class LoanTypeReactiveRepositoryAdapter extends
        ReactiveAdapterOperations<LoanType, LoanTypeEntity, Long, LoanTypeReactiveRepository>
        implements LoanTypeRepository {
    public LoanTypeReactiveRepositoryAdapter(LoanTypeReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, LoanType.class));
    }

    @Override
    public Mono<LoanType> findByName(String name) {
        return repository.findByName(name)
                .map(this::toEntity);
    }
}
