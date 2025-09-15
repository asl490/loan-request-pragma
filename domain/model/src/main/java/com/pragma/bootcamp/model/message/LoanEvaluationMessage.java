package com.pragma.bootcamp.model.message;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoanEvaluationMessage {
    private LoanRequestDetails loanRequest;
    private BigDecimal baseSalary;
    private List<ActiveLoan> activeLoans;
}
