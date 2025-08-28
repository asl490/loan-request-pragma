package com.pragma.bootcamp.model.requestloan;

import java.math.BigDecimal;

import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.requeststatus.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RequestLoan {
    private Long id;
    private BigDecimal amount;
    private Integer loanTerm;
    private String email;
    private String dni;
    private RequestStatus requestStatus;
    private LoanType loanType;

}
