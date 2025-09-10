package com.pragma.bootcamp.r2dbc;

import com.pragma.bootcamp.model.requeststatus.RequestStatus;
import com.pragma.bootcamp.r2dbc.adapter.RequestStatusReactiveRepository;
import com.pragma.bootcamp.r2dbc.adapter.RequestStatusReactiveRepositoryAdapter;
import com.pragma.bootcamp.r2dbc.entity.RequestStatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class RequestStatusReactiveRepositoryAdapterTest {

    private RequestStatusReactiveRepository repository;
    private ObjectMapper objectMapper;
    private RequestStatusReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(RequestStatusReactiveRepository.class);
        objectMapper = mock(ObjectMapper.class);
        adapter = new RequestStatusReactiveRepositoryAdapter(repository, objectMapper);
    }

    @Test
    void findByName_ShouldReturnMappedRequestStatus() {
        // Arrange
        String name = "PENDING";
        RequestStatusEntity entity = new RequestStatusEntity();
        entity.setId(1L);
        entity.setName(name);

        RequestStatus expectedStatus = RequestStatus.builder()
                .id(1L)
                .name(name)
                .build();

        when(repository.findByName(name)).thenReturn(Mono.just(entity));
        when(objectMapper.map(entity, RequestStatus.class)).thenReturn(expectedStatus);

        // Act & Assert
        StepVerifier.create(adapter.findByName(name))
                .expectNext(expectedStatus)
                .verifyComplete();

        verify(repository, times(1)).findByName(name);
        verify(objectMapper, times(1)).map(entity, RequestStatus.class);
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNotFound() {
        // Arrange
        String name = "NOT_FOUND";
        when(repository.findByName(name)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(adapter.findByName(name))
                .verifyComplete();

        verify(repository, times(1)).findByName(name);
        verify(objectMapper, never()).map(any(), eq(RequestStatus.class));
    }
}
