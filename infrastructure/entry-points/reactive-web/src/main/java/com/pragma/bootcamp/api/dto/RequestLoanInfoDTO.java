package com.pragma.bootcamp.api.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class RequestLoanInfoDTO {
    private Long id;
    private BigDecimal amount;
    private String email;
    private String dni;
    private Integer term;
    private Long requestStatus;
    private Long loanType;
    private BigDecimal approvedLoansSum;
    ;

}
