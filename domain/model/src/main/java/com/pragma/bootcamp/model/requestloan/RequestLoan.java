package com.pragma.bootcamp.model.requestloan;

import java.math.BigDecimal;

import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.requeststatus.RequestStatus;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class RequestLoan {
    private Long id;
    private BigDecimal amount;
    private String email;
    private String dni;
    private String currency;
    private Integer term;
    private RequestStatus requestStatus;
    private LoanType loanType;

}
