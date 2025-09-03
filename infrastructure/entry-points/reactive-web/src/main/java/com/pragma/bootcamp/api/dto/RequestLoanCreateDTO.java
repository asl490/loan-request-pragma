package com.pragma.bootcamp.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestLoanCreateDTO {

    @NotNull(message = "El monto del préstamo no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El monto debe tener hasta 10 dígitos enteros y 2 decimales")
    private BigDecimal amount;

    @NotNull(message = "El plazo del préstamo no puede ser nulo")
    @Min(value = 1, message = "El plazo del préstamo debe ser al menos de 1 mes")
    @Max(value = 360, message = "El plazo del préstamo no puede exceder los 360 meses")
    private Integer term;

    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "El correo electrónico no tiene un formato válido")
    private String email;

    @NotBlank(message = "El DNI no puede estar vacío")
    @Size(min = 6, max = 15, message = "El DNI debe tener entre 6 y 15 caracteres")
    private String dni;

    @NotNull(message = "El tipo de préstamo no puede ser nulo")
    @Min(value = 1, message = "El ID del tipo de préstamo debe ser al menos 1")
    private Long loanType;

}