package com.pragma.bootcamp.api;

import com.pragma.bootcamp.api.dto.PageRequestDTO;
import com.pragma.bootcamp.api.dto.PageResponseDTO;
import com.pragma.bootcamp.api.dto.RequestLoanCreateDTO;
import com.pragma.bootcamp.api.dto.RequestLoanDTO;
import com.pragma.bootcamp.api.mapper.RequestLoanMapper;
import com.pragma.bootcamp.common.PageRequest;
import com.pragma.bootcamp.common.PageResponse;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.usecase.requestloan.RequestLoanUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final RequestLoanUseCase requestLoanUseCase;
    private final Validator validator;
    private final RequestLoanMapper requestLoanMapper;

    public Mono<ServerResponse> createRequestLoan(ServerRequest serverRequest) {
        log.trace("Received request to save a new loan.");
        return serverRequest
                .bodyToMono(RequestLoanCreateDTO.class)
                .doOnNext(dto -> log.trace("Request body: {}", dto))
                .doOnNext(this::validate)
                .map(requestLoanMapper::toDomain)
                .flatMap(requestLoanUseCase::createRequestLoan)
                .map(requestLoanMapper::toDTO)
                .flatMap(createdRequest ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(createdRequest)
                );

    }

    public Mono<ServerResponse> searchWithFilters(ServerRequest serverRequest) {
        log.trace("Received request to search loans with filters.");
        return serverRequest
                .bodyToMono(PageRequestDTO.class)
                .doOnNext(dto -> log.trace("Search request body: {}", dto))
                .doOnNext(this::validatePageRequest)
                .map(this::toPageRequestDomain)
                .flatMap(requestLoanUseCase::execute)
                .map(this::toPageResponseDTO)
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                )
                ;
    }

    private PageRequest toPageRequestDomain(PageRequestDTO dto) {
        return PageRequest.builder()
                .page(dto.getPage())
                .size(dto.getSize())
//                .sortBy(dto.getSortBy())
//                .sortDirection(dto.getSortDirection())
                .filters(dto.getFilters())
                .build();
    }

    private PageResponseDTO<RequestLoanDTO> toPageResponseDTO(
            PageResponse<RequestLoan> pageResponse) {

        List<RequestLoanDTO> content = pageResponse.getContent().stream()
                .map(requestLoanMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<RequestLoanDTO>builder()
                .content(content)
                .page(pageResponse.getPage())
                .size(pageResponse.getSize())
                .totalElements(pageResponse.getTotalElements())
                .totalPages(pageResponse.getTotalPages())
                .first(pageResponse.isFirst())
                .last(pageResponse.isLast())
                .build();
    }

    private void validatePageRequest(PageRequestDTO pageRequestDTO) {
        Set<ConstraintViolation<PageRequestDTO>> violations = validator.validate(pageRequestDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        if (pageRequestDTO.getPage() < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (pageRequestDTO.getSize() <= 0 || pageRequestDTO.getSize() > 1000) {
            throw new IllegalArgumentException("Page size must be between 1 and 1000");
        }
    }

    private void validate(RequestLoanCreateDTO requestLoanDTO) {
        Set<ConstraintViolation<RequestLoanCreateDTO>> violations = validator.validate(requestLoanDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

}
