package com.pragma.bootcamp.usecase.requestloan;

import com.pragma.bootcamp.common.PageRequest;
import com.pragma.bootcamp.common.PageResponse;
import com.pragma.bootcamp.model.client.Client;
import com.pragma.bootcamp.model.client.exception.ClientNotFoundException;
import com.pragma.bootcamp.model.client.gateways.ClientRepository;
import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.loantype.exception.LoanAmountOutOfRangeException;
import com.pragma.bootcamp.model.loantype.exception.LoanTypeNotFoundException;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.RequestLoanInfo;
import com.pragma.bootcamp.model.requestloan.gateways.LoanEvaluationGateway;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requeststatus.RequestStatus;
import com.pragma.bootcamp.model.requeststatus.exception.RequestStatusNotFoundException;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RequestLoanUseCaseTest {

    @Mock
    private RequestLoanRepository loanRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private RequestStatusRepository requestStatusRepository;

    @Mock
    private LoanEvaluationGateway loanEvaluationGateway;

    @InjectMocks
    private RequestLoanUseCase requestLoanUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test for creating a loan request successfully")
    void testCreateRequestLoanSuccessfully() {
        RequestLoan requestLoan = RequestLoan.builder()
                .dni("123456789")
                .amount(BigDecimal.valueOf(10000))
                .loanType(LoanType.builder().id(1L).build())
                .build();

        Client client = Client.builder()
                .document("123456789")
                .email("test@example.com")
                .build();

        LoanType loanType = LoanType.builder()
                .id(1L)
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(20000))
                .build();

        RequestStatus requestStatus = RequestStatus.builder()
                .id(1L)
                .name("PENDING")
                .build();

        when(clientRepository.getClientByDni("123456789")).thenReturn(Mono.just(client));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
        when(requestStatusRepository.findByName("PENDING")).thenReturn(Mono.just(requestStatus));
        when(loanRepository.createLoan(any(RequestLoan.class))).thenReturn(Mono.just(requestLoan));
        when(loanRepository.findApprovedLoansByDni("123456789")).thenReturn(Flux.empty());
        when(loanEvaluationGateway.sendForEvaluation(any())).thenReturn(Mono.empty());

        StepVerifier.create(requestLoanUseCase.createRequestLoan(requestLoan))
                .expectNext(requestLoan)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for client not found when creating a loan request")
    void testCreateRequestLoanClientNotFound() {
        RequestLoan requestLoan = RequestLoan.builder()
                .dni("123456789")
                .amount(BigDecimal.valueOf(10000))
                .loanType(LoanType.builder().id(1L).build())
                .build();

        when(clientRepository.getClientByDni("123456789")).thenReturn(Mono.empty());

        StepVerifier.create(requestLoanUseCase.createRequestLoan(requestLoan))
                .expectError(ClientNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Test for loan type not found when creating a loan request")
    void testCreateRequestLoanLoanTypeNotFound() {
        RequestLoan requestLoan = RequestLoan.builder()
                .dni("123456789")
                .amount(BigDecimal.valueOf(10000))
                .loanType(LoanType.builder().id(1L).build())
                .build();

        Client client = Client.builder()
                .document("123456789")
                .email("test@example.com")
                .build();

        when(clientRepository.getClientByDni("123456789")).thenReturn(Mono.just(client));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(requestLoanUseCase.createRequestLoan(requestLoan))
                .expectError(LoanTypeNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Test for loan amount out of range when creating a loan request")
    void testCreateRequestLoanAmountOutOfRange() {
        RequestLoan requestLoan = RequestLoan.builder()
                .dni("123456789")
                .amount(BigDecimal.valueOf(500))
                .loanType(LoanType.builder().id(1L).build())
                .build();

        Client client = Client.builder()
                .document("123456789")
                .email("test@example.com")
                .build();

        LoanType loanType = LoanType.builder()
                .id(1L)
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(20000))
                .build();

        when(clientRepository.getClientByDni("123456789")).thenReturn(Mono.just(client));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));

        StepVerifier.create(requestLoanUseCase.createRequestLoan(requestLoan))
                .expectError(LoanAmountOutOfRangeException.class)
                .verify();
    }

    @Test
    @DisplayName("Test for request status not found when creating a loan request")
    void testCreateRequestLoanRequestStatusNotFound() {
        RequestLoan requestLoan = RequestLoan.builder()
                .dni("123456789")
                .amount(BigDecimal.valueOf(10000))
                .loanType(LoanType.builder().id(1L).build())
                .build();

        Client client = Client.builder()
                .document("123456789")
                .email("test@example.com")
                .build();

        LoanType loanType = LoanType.builder()
                .id(1L)
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(20000))
                .build();

        when(clientRepository.getClientByDni("123456789")).thenReturn(Mono.just(client));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
        when(requestStatusRepository.findByName("PENDING")).thenReturn(Mono.empty());

        StepVerifier.create(requestLoanUseCase.createRequestLoan(requestLoan))
                .expectError(RequestStatusNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Test for getting a loan request by id")
    void testGetById() {
        RequestLoan requestLoan = RequestLoan.builder().id(1L).build();
        when(loanRepository.findRequestLoanById(1L)).thenReturn(Mono.just(requestLoan));

        StepVerifier.create(requestLoanUseCase.getById(1L))
                .expectNext(requestLoan)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for finding approved loans by dni")
    void testFindApproveLoansByDni() {
        RequestLoan requestLoan = RequestLoan.builder().dni("123456789").build();
        when(loanRepository.findApprovedLoansByDni("123456789")).thenReturn(Flux.just(requestLoan));

        StepVerifier.create(requestLoanUseCase.findApproveLoansByDni("123456789"))
                .expectNext(requestLoan)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for executing with filters")
    void testExecute() {
        PageRequest pageRequest = new PageRequest(0, 10, Collections.emptyList());
        PageResponse<RequestLoan> pageResponse = new PageResponse<>(Collections.emptyList(), 0,0,0,0,true,true);
        when(loanRepository.findWithFilters(pageRequest)).thenReturn(Mono.just(pageResponse));

        StepVerifier.create(requestLoanUseCase.execute(pageRequest))
                .expectNext(pageResponse)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for executing with filters info")
    void testExecuteInfo() {
        PageRequest pageRequest = new PageRequest(0, 10, Collections.emptyList());
        PageResponse<RequestLoanInfo> pageResponse = new PageResponse<>(Collections.emptyList(), 0,0,0,0,true,true);
        when(loanRepository.findWithFiltersInfo(pageRequest)).thenReturn(Mono.just(pageResponse));

        StepVerifier.create(requestLoanUseCase.executeInfo(pageRequest))
                .expectNext(pageResponse)
                .verifyComplete();
    }
}