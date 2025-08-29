package com.pragma.bootcamp.r2dbc.adapter;

import com.pragma.bootcamp.utils.gateways.TransactionalGateway;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.r2dbc.entity.RequestLoanEntity;
import com.pragma.bootcamp.r2dbc.helper.ReactiveAdapterOperations;
import com.pragma.bootcamp.r2dbc.mapper.RequestLoanMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class RequestLoanReactiveRepositoryAdapter extends
        ReactiveAdapterOperations<RequestLoan, RequestLoanEntity, Long, RequestLoanReactiveRepository>
        implements RequestLoanRepository {
    public RequestLoanReactiveRepositoryAdapter(RequestLoanReactiveRepository repository, ObjectMapper mapper,
                                                RequestLoanMapper requestLoanMapper, TransactionalGateway transactionalGateway) {
        super(repository, mapper, d -> mapper.map(d, RequestLoan.class));
        this.requestLoanMapper = requestLoanMapper;
        this.transactionalGateway = transactionalGateway;
    }

    private final RequestLoanMapper requestLoanMapper;
    private final TransactionalGateway transactionalGateway;

    @Override
    public Flux<RequestLoan> getAll() {
        return repository.findAll().map(requestLoanMapper::toDomain);
    }

//    @Override
//    public Mono<RequestLoan> update(RequestLoan requestLoanUpdate) {
//        return super.save(requestLoanUpdate);
//    }

    @Override
    public Mono<Void> delete(Long idRequestLoan) {
        return repository.deleteById(idRequestLoan);
    }

    @Override
    public Mono<RequestLoan> createLoan(RequestLoan requestLoan) {
        log.trace("Saving new RequestLoan with document: {}", requestLoan.toString());
        RequestLoanEntity requestLoanEntity = requestLoanMapper.toEntity(requestLoan);

        return transactionalGateway.doInTransaction(repository.save(requestLoanEntity)
                .doOnNext(savedEntity -> log.info("Entity after save: {}", savedEntity)) // verifica el ID aquí
                .map(requestLoanMapper::toDomain)
                .doOnSuccess(savedRequestLoan -> log.info("Successfully created RequestLoan with ID: {}",
                        savedRequestLoan.getId())));
    }

}
