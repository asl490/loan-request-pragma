package com.pragma.bootcamp.usecase.loantype;

import com.pragma.bootcamp.model.client.exception.ClientNotFoundException;
import com.pragma.bootcamp.model.client.gateways.ClientRepository;
import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.loantype.exception.LoanAmountOutOfRangeException;
import com.pragma.bootcamp.model.loantype.exception.LoanTypeNotFoundException;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requeststatus.RequestStatus;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import com.pragma.bootcamp.usecase.requestloan.RequestLoanUseCase;
import com.pragma.bootcamp.utils.gateways.TransactionalGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class RequestLoanUseCaseTest {

    private RequestLoanRepository loanRepository;
    private TransactionalGateway transactionalGateway;
    private ClientRepository clientRepository;
    private LoanTypeRepository loanTypeRepository;
    private RequestStatusRepository requestStatusRepository;
    private RequestLoanUseCase useCase;

    @BeforeEach
    void setUp() {
        loanRepository = mock(RequestLoanRepository.class);
        transactionalGateway = mock(TransactionalGateway.class);
        clientRepository = mock(ClientRepository.class);
        loanTypeRepository = mock(LoanTypeRepository.class);
        requestStatusRepository = mock(RequestStatusRepository.class);

        useCase = new RequestLoanUseCase(
                loanRepository,
                transactionalGateway,
                clientRepository,
                loanTypeRepository,
                requestStatusRepository
        );

        // Simplifica las pruebas: ejecuta directamente el flujo pasado
        when(transactionalGateway.doInTransaction(any()))
                .thenAnswer(invocation -> {
                    var supplier = invocation.getArgument(0);
                    return ((Mono<?>) supplier);
                });
    }

    @Test
    void shouldCreateRequestLoanSuccessfully() {
        // Arrange
        var dni = "12345678";
        var email = "user@example.com";
        var amount = BigDecimal.valueOf(5000);
        var loanType = LoanType.builder()
                .id(1L)
                .name("Personal")
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(10000))
                .build();
        var requestStatus = RequestStatus.builder()
                .id(1L)
                .description("PENDING")
                .build();

        var requestLoan = RequestLoan.builder()
                .dni(dni)
                .amount(amount)
                .loanType(LoanType.builder().id(1L).build())
                .build();

        when(clientRepository.getEmailByDni(dni)).thenReturn(Mono.just(email));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
        when(requestStatusRepository.findById(1L)).thenReturn(Mono.just(requestStatus));
        when(loanRepository.createLoan(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act & Assert
        StepVerifier.create(useCase.createRequestLoan(requestLoan))
                .expectNextMatches(result ->
                        result.getEmail().equals(email) &&
                                result.getLoanType().getName().equals("Personal") &&
                                result.getRequestStatus().getDescription().equals("PENDING")
                )
                .verifyComplete();

        verify(loanRepository).createLoan(any(RequestLoan.class));
    }

    @Test
    void shouldReturnClientNotFoundException() {
        var dni = "12345678";
        var requestLoan = RequestLoan.builder()
                .dni(dni)
                .loanType(LoanType.builder().id(1L).build())
                .amount(BigDecimal.valueOf(3000))
                .build();

        when(clientRepository.getEmailByDni(dni)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.createRequestLoan(requestLoan))
                .expectErrorMatches(throwable ->
                        throwable instanceof ClientNotFoundException &&
                                throwable.getMessage().contains(dni))
                .verify();
    }

    @Test
    void shouldReturnLoanTypeNotFoundException() {
        var requestLoan = RequestLoan.builder()
                .dni("123")
                .loanType(LoanType.builder().id(99L).build())
                .amount(BigDecimal.valueOf(3000))
                .build();

        when(clientRepository.getEmailByDni("123")).thenReturn(Mono.just("user@example.com"));
        when(loanTypeRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.createRequestLoan(requestLoan))
                .expectError(LoanTypeNotFoundException.class)
                .verify();
    }

    @Test
    void shouldReturnLoanAmountOutOfRangeException() {
        var loanType = LoanType.builder()
                .id(1L)
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(2000))
                .name("Micro")
                .build();

        var requestLoan = RequestLoan.builder()
                .dni("123")
                .loanType(LoanType.builder().id(1L).build())
                .amount(BigDecimal.valueOf(5000)) // fuera del rango
                .build();

        when(clientRepository.getEmailByDni("123")).thenReturn(Mono.just("user@example.com"));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));

        StepVerifier.create(useCase.createRequestLoan(requestLoan))
                .expectError(LoanAmountOutOfRangeException.class)
                .verify();
    }

    @Test
    void shouldReturnIllegalStateWhenStatusNotFound() {
        var requestLoan = RequestLoan.builder()
                .dni("123")
                .loanType(LoanType.builder().id(1L).build())
                .amount(BigDecimal.valueOf(1500))
                .build();

        var loanType = LoanType.builder()
                .id(1L)
                .name("Simple")
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(2000))
                .build();

        when(clientRepository.getEmailByDni("123")).thenReturn(Mono.just("user@example.com"));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
        when(requestStatusRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.createRequestLoan(requestLoan))
                .expectErrorMatches(error ->
                        error instanceof IllegalStateException &&
                                error.getMessage().contains("Request status not found"))
                .verify();
    }
}
