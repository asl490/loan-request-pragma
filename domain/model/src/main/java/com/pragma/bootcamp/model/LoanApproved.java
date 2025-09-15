package com.pragma.bootcamp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanApproved {
    private Long requestId;
    private String status;
    private BigDecimal monthlyPayment;
    private BigDecimal availableCapacity;
    private BigDecimal totalRequestedAmount;
    private Integer termMonths;
    private String email;
    private List<PaymentSchedule> paymentSchedule;
}
