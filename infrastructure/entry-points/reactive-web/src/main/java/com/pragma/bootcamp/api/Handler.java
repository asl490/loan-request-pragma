package com.pragma.bootcamp.api;

import com.pragma.bootcamp.api.dto.ErrorResponse;
import com.pragma.bootcamp.api.dto.RequestLoanCreateDTO;
import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.usecase.requestloan.RequestLoanUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final RequestLoanUseCase requestLoanUseCase;
    private final Validator validator;

    public Mono<ServerResponse> createRequestLoan(ServerRequest serverRequest) {
        log.trace("Received request to save a new loan.");
        return serverRequest
                .bodyToMono(RequestLoanCreateDTO.class)
                .doOnNext(dto -> log.trace("Request body: {}", dto))
                .doOnNext(this::validate)
                .map(this::mapToDomain)
                .flatMap(requestLoanUseCase::createRequestLoan)
                .flatMap(createdRequest ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(createdRequest)
                )
                .onErrorResume(
                    ConstraintViolationException.class, this::handleValidationException
                );
    }

    private void validate(RequestLoanCreateDTO requestLoanDTO) {
        Set<ConstraintViolation<RequestLoanCreateDTO>> violations = validator.validate(requestLoanDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private RequestLoan mapToDomain(RequestLoanCreateDTO dto) {
        RequestLoan requestLoan = new RequestLoan();
        requestLoan.setAmount(dto.getAmount());
        requestLoan.setTerm(dto.getLoanTerm());
        requestLoan.setDni(dto.getDni());
        requestLoan.setEmail(dto.getEmail());

        LoanType loanType = new LoanType();
        loanType.setId(dto.getLoanType());
        requestLoan.setLoanType(loanType);

        return requestLoan;
    }

    private Mono<ServerResponse> handleValidationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        log.error("Validation errors: {}", errors);
        ErrorResponse errorResponse = ErrorResponse.builder()

                .errorCode(HttpStatus.BAD_REQUEST.name())
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

}
