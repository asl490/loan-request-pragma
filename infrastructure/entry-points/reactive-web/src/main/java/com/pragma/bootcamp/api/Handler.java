package com.pragma.bootcamp.api;

import com.pragma.bootcamp.api.dto.*;
import com.pragma.bootcamp.api.mapper.RequestLoanMapper;
import com.pragma.bootcamp.common.PageRequest;
import com.pragma.bootcamp.common.PageResponse;
import com.pragma.bootcamp.exception.ForbiddenException;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.usecase.requestloan.RequestLoanUseCase;
import com.pragma.bootcamp.usecase.requestloan.UpdateStateRequestLoanUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final RequestLoanUseCase requestLoanUseCase;
    private final UpdateStateRequestLoanUseCase updateStateRequestLoanUseCase;
    private final Validator validator;
    private final RequestLoanMapper requestLoanMapper;

//    @PreAuthorize("hasRole('CLIENTE')")
    public Mono<ServerResponse> createRequestLoan(ServerRequest serverRequest) {
        log.trace("Received request to save a new loan.");

        return getAuthenticatedUser()
                .flatMap(user -> {
                    log.trace("Authenticated user: {}", user);
                    return serverRequest
                            .bodyToMono(RequestLoanCreateDTO.class)
                            .doOnNext(dto -> log.trace("Request body: {}", dto))
                            .flatMap(dto -> validateDniMatch(dto, user)) // aquí usamos la validación
                            .doOnNext(this::validate)
                            .map(requestLoanMapper::toDomain)
                            .flatMap(requestLoanUseCase::createRequestLoan)
                            .map(requestLoanMapper::toDTO)
                            .flatMap(createdRequest ->
                                    ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(createdRequest)
                            );
                });

    }

//    @PreAuthorize("hasRole('ASESOR')")
    public Mono<ServerResponse> updateRequestState(ServerRequest serverRequest) {
        log.trace("Received request to update a loan state.");
        Long id = Long.valueOf(serverRequest.pathVariable("id"));

        return serverRequest.bodyToMono(UpdateStateDto.class)
                .doOnNext(dto -> log.trace("Request body for state update: {}", dto))
                .flatMap(dto -> updateStateRequestLoanUseCase.updateState(id, dto.getState()))
                .map(requestLoanMapper::toResponseTO)
                .flatMap(updatedRequest ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(updatedRequest)
                );
    }

//    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
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

//    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public Mono<ServerResponse> searchWithFiltersInfo(ServerRequest serverRequest) {
        log.trace("Received request to search loans with filters.");
        return serverRequest
                .bodyToMono(PageRequestDTO.class)
                .doOnNext(dto -> log.trace("Search request body: {}", dto))
                .doOnNext(this::validatePageRequest)
                .map(this::toPageRequestDomain)
                .flatMap(requestLoanUseCase::executeInfo)
                .doOnNext(dto -> log.trace("Search request body: {}", dto))
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                )
                ;
    }
    public Mono<ServerResponse> findApprovedLoans(ServerRequest serverRequest) {
        String dni = (serverRequest.pathVariable("dni"));

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestLoanUseCase.findApproveLoansByDni(dni).map(requestLoanMapper::toDTO), RequestLoanDTO.class);
//        return requestLoanUseCase.findApproveLoansByDni(dni)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(userUseCase.getAll().map(userRestMapper::toUserDTO),
//                        UserDTO.class);
    }

//    private Flux<RequestLoanDTO> findApprovedLoans(ServerRequest serverRequest) {
//
//            return serverRequest.requestLoanUseCase.findApproveLoansByDni(dnis).map(requestLoanMapper::toDTO);
//
//    }

    private PageRequest toPageRequestDomain(PageRequestDTO dto) {
        return PageRequest.builder()
                .page(dto.getPage())
                .size(dto.getSize())
//                .sortBy(dto.getSortBy())
//                .sortDirection(dto.getSortDirection())
                .filters(dto.getFilters())
                .build();
    }

    private Mono<UserTokenData> getAuthenticatedUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(UsernamePasswordAuthenticationToken.class)
                .map(auth -> (UserTokenData) auth.getPrincipal());
    }

    private Mono<RequestLoanCreateDTO> validateDniMatch(RequestLoanCreateDTO dto, UserTokenData user) {
        if (dto.getDni() == null || !dto.getDni().equals(user.document())) {
            log.warn("DNI from request ({}) does not match authenticated user's document ({})", dto.getDni(), user.document());
            return Mono.error(new ForbiddenException("DNI does not match the authenticated user's document"));
        }
        return Mono.just(dto);
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

