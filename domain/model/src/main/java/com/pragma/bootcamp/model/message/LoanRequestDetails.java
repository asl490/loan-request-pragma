package com.pragma.bootcamp.model.message;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoanRequestDetails {
    private Long id;
    private BigDecimal amount;
    private String dni;
    private Integer termMonths;
    private Long loanTypeId;
    private String email;
    private Double annualInterestRate;
}
