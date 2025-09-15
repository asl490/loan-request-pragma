package com.pragma.bootcamp.model.message;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ActiveLoan {
    private BigDecimal amount;
    private Double annualInterestRate;
    private Integer termMonths;
}
