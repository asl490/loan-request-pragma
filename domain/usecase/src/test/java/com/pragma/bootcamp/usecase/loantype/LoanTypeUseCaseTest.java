package com.pragma.bootcamp.usecase.loantype;

import com.pragma.bootcamp.exception.BusinessException;
import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class LoanTypeUseCaseTest {

    private LoanTypeRepository loanTypeRepository;
    private LoanTypeUseCase loanTypeUseCase;

    @BeforeEach
    void setUp() {
        loanTypeRepository = mock(LoanTypeRepository.class);
        loanTypeUseCase = new LoanTypeUseCase(loanTypeRepository);
    }

    @Test
    void shouldReturnLoanTypeWhenExists() {
        // Arrange
        Long id = 1L;
        LoanType expectedLoanType = LoanType.builder()
                .id(id)
                .name("Personal")
                .build();

        when(loanTypeRepository.findById(id)).thenReturn(Mono.just(expectedLoanType));

        // Act & Assert
        StepVerifier.create(loanTypeUseCase.getTypeLoanById(id))
                .expectNext(expectedLoanType)
                .verifyComplete();

        verify(loanTypeRepository).findById(id);
    }

    @Test
    void shouldThrowBusinessExceptionWhenLoanTypeNotFound() {
        // Arrange
        Long id = 2L;
        when(loanTypeRepository.findById(id)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loanTypeUseCase.getTypeLoanById(id))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                throwable.getMessage().equals("The requested loan was not found."))
                .verify();

        verify(loanTypeRepository).findById(id);
    }
}
