package com.pragma.bootcamp.model.requestloan;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class RequestLoanInfo {
    private Long id;
    private BigDecimal amount;
    private String email;
    private String dni;
    private Integer term;
    private Long requestStatus;
    private Long loanType;
    private BigDecimal approvedLoansSum;;

}
