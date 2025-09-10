package com.pragma.bootcamp.usecase.requestloan;

import com.pragma.bootcamp.model.client.exception.ClientNotFoundException;
import com.pragma.bootcamp.model.client.gateways.ClientRepository;
import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.loantype.exception.LoanAmountOutOfRangeException;
import com.pragma.bootcamp.model.loantype.exception.LoanTypeNotFoundException;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requeststatus.RequestStatus;
import com.pragma.bootcamp.model.requeststatus.exception.RequestStatusNotFoundException;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

class RequestLoanUseCaseTest {

        private RequestLoanRepository loanRepository;
        private ClientRepository clientRepository;
        private LoanTypeRepository loanTypeRepository;
        private RequestStatusRepository requestStatusRepository;

        private RequestLoanUseCase requestLoanUseCase;

        @BeforeEach
        void setUp() {
                loanRepository = Mockito.mock(RequestLoanRepository.class);
                clientRepository = Mockito.mock(ClientRepository.class);
                loanTypeRepository = Mockito.mock(LoanTypeRepository.class);
                requestStatusRepository = Mockito.mock(RequestStatusRepository.class);

                requestLoanUseCase = new RequestLoanUseCase(
                                loanRepository, clientRepository, loanTypeRepository, requestStatusRepository);
        }

        // ----------------------------------------------------------------------------------------------------
        // TEST: createRequestLoan - Cliente no encontrado
        // ----------------------------------------------------------------------------------------------------
        @Test
        void createRequestLoan_ClientNotFound_ShouldThrow() {
                // Arrange
                RequestLoan requestLoan = RequestLoan.builder()
                                .dni("999")
                                .amount(BigDecimal.valueOf(1000))
                                .loanType(LoanType.builder().id(1L).build())
                                .build();

                Mockito.when(clientRepository.getEmailByDni("999"))
                                .thenReturn(Mono.empty());

                // Act
                Mono<RequestLoan> result = requestLoanUseCase.createRequestLoan(requestLoan);

                // Assert
                StepVerifier.create(result)
                                .expectErrorSatisfies(throwable -> {
                                        assert throwable instanceof ClientNotFoundException;
                                })
                                .verify();
        }

        // ----------------------------------------------------------------------------------------------------
        // TEST: createRequestLoan - Tipo de préstamo no encontrado
        // ----------------------------------------------------------------------------------------------------
        @Test
        void createRequestLoan_LoanTypeNotFound_ShouldThrow() {
                // Arrange
                RequestLoan requestLoan = RequestLoan.builder()
                                .dni("123")
                                .amount(BigDecimal.valueOf(5000))
                                .loanType(LoanType.builder().id(999L).build())
                                .build();

                Mockito.when(clientRepository.getEmailByDni("123"))
                                .thenReturn(Mono.just("client@example.com"));

                Mockito.when(loanTypeRepository.findById(999L))
                                .thenReturn(Mono.empty());

                // Act
                Mono<RequestLoan> result = requestLoanUseCase.createRequestLoan(requestLoan);

                // Assert
                StepVerifier.create(result)
                                .expectError(LoanTypeNotFoundException.class)
                                .verify();
        }

        // ----------------------------------------------------------------------------------------------------
        // TEST: createRequestLoan - Monto fuera de rango
        // ----------------------------------------------------------------------------------------------------
        @Test
        void createRequestLoan_AmountOutOfRange_ShouldThrow() {
                // Arrange
                RequestLoan requestLoan = RequestLoan.builder()
                                .dni("123")
                                .amount(BigDecimal.valueOf(15000)) // fuera del rango
                                .loanType(LoanType.builder().id(1L).build())
                                .build();

                LoanType loanType = LoanType.builder()
                                .id(1L)
                                .minAmount(BigDecimal.valueOf(1000))
                                .maxAmount(BigDecimal.valueOf(10000))
                                .build();

                Mockito.when(clientRepository.getEmailByDni("123"))
                                .thenReturn(Mono.just("client@example.com"));

                Mockito.when(loanTypeRepository.findById(1L))
                                .thenReturn(Mono.just(loanType));

                // No pasa el filtro de rango → switchIfEmpty
                Mockito.when(requestStatusRepository.findByName("PENDING"))
                                .thenReturn(Mono.just(RequestStatus.builder().id(1L).name("PENDING").build()));

                // Act
                Mono<RequestLoan> result = requestLoanUseCase.createRequestLoan(requestLoan);

                // Assert
                StepVerifier.create(result)
                                .expectError(LoanAmountOutOfRangeException.class)
                                .verify();
        }

        // ----------------------------------------------------------------------------------------------------
        // TEST: createRequestLoan - Estado PENDING no encontrado
        // ----------------------------------------------------------------------------------------------------
        @Test
        void createRequestLoan_RequestStatusNotFound_ShouldThrow() {
                // Arrange
                RequestLoan requestLoan = RequestLoan.builder()
                                .dni("123")
                                .amount(BigDecimal.valueOf(5000))
                                .loanType(LoanType.builder().id(1L).build())
                                .build();

                LoanType validLoanType = LoanType.builder()
                                .id(1L)
                                .minAmount(BigDecimal.valueOf(1000))
                                .maxAmount(BigDecimal.valueOf(10000))
                                .build();

                Mockito.when(clientRepository.getEmailByDni("123"))
                                .thenReturn(Mono.just("client@example.com"));

                Mockito.when(loanTypeRepository.findById(1L))
                                .thenReturn(Mono.just(validLoanType));

                Mockito.when(requestStatusRepository.findByName("PENDING"))
                                .thenReturn(Mono.empty());

                // Act
                Mono<RequestLoan> result = requestLoanUseCase.createRequestLoan(requestLoan);

                // Assert
                StepVerifier.create(result)
                                .expectError(RequestStatusNotFoundException.class)
                                .verify();
        }

}