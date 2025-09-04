package com.pragma.bootcamp.r2dbc;

import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.r2dbc.adapter.RequestLoanReactiveRepository;
import com.pragma.bootcamp.r2dbc.adapter.RequestLoanReactiveRepositoryAdapter;
import com.pragma.bootcamp.r2dbc.entity.RequestLoanEntity;
import com.pragma.bootcamp.r2dbc.mapper.RequestLoanEntityMapper;
import com.pragma.bootcamp.utils.gateways.TransactionalGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

class RequestLoanReactiveRepositoryAdapterTest {

    private RequestLoanReactiveRepository repository;
    private ObjectMapper objectMapper;
    private RequestLoanEntityMapper mapper;
    private TransactionalGateway transactionalGateway;
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    private RequestLoanReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(RequestLoanReactiveRepository.class);
        objectMapper = mock(ObjectMapper.class);
        mapper = mock(RequestLoanEntityMapper.class);
        transactionalGateway = mock(TransactionalGateway.class);
        r2dbcEntityTemplate = mock(R2dbcEntityTemplate.class);

        adapter = new RequestLoanReactiveRepositoryAdapter(repository, objectMapper, mapper, transactionalGateway, r2dbcEntityTemplate);
    }

    @Test
    void shouldCreateLoanSuccessfully() {
        // Arrange
        RequestLoan requestLoan = RequestLoan.builder()
                .id(null)
                .dni("12345678")
                .amount(BigDecimal.valueOf(5000))
                .build();

        RequestLoanEntity entityToSave = RequestLoanEntity.builder()
                .dni("12345678")
                .amount(BigDecimal.valueOf(5000))
                .build();

        RequestLoanEntity savedEntity = RequestLoanEntity.builder()
                .id(1L)
                .dni("12345678")
                .amount(BigDecimal.valueOf(5000))
                .build();

        RequestLoan domainSaved = RequestLoan.builder()
                .id(1L)
                .dni("12345678")
                .amount(BigDecimal.valueOf(5000))
                .build();

        when(mapper.toEntity(requestLoan)).thenReturn(entityToSave);
        when(repository.save(entityToSave)).thenReturn(Mono.just(savedEntity));
        when(mapper.toDomain(savedEntity)).thenReturn(domainSaved);

        // Act & Assert
        StepVerifier.create(adapter.createLoan(requestLoan))
                .expectNextMatches(result ->
                        result.getId() == 1L &&
                                result.getDni().equals("12345678") &&
                                result.getAmount().equals(BigDecimal.valueOf(5000))
                )
                .verifyComplete();
    }

    @Test
    void shouldGetAllLoans() {
        RequestLoanEntity entity1 = RequestLoanEntity.builder().id(1L).dni("111").amount(BigDecimal.valueOf(1000)).build();
        RequestLoanEntity entity2 = RequestLoanEntity.builder().id(2L).dni("222").amount(BigDecimal.valueOf(2000)).build();

        RequestLoan domain1 = RequestLoan.builder().id(1L).dni("111").amount(BigDecimal.valueOf(1000)).build();
        RequestLoan domain2 = RequestLoan.builder().id(2L).dni("222").amount(BigDecimal.valueOf(2000)).build();

        when(repository.findAll()).thenReturn(Flux.just(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(domain1);
        when(mapper.toDomain(entity2)).thenReturn(domain2);

        StepVerifier.create(adapter.getAll())
                .expectNext(domain1)
                .expectNext(domain2)
                .verifyComplete();
    }

    @Test
    void shouldDeleteLoanById() {
        Long id = 99L;

        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.delete(id))
                .verifyComplete();

        verify(repository).deleteById(id);
    }
}
