package com.pragma.bootcamp.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "Código de error del negocio", example = "CLIENT_NOT_FOUND")
    private String errorCode;
    @Schema(description = "Mensaje principal del error", example = "El cliente no fue encontrado")
    private String message;
    @Schema(description = "Fecha y hora del error")
    private LocalDateTime timestamp;
    @Schema(description = "Errores específicos de validación, si los hay")
    private List<String> errors;

}