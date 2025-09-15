package com.pragma.bootcamp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSchedule {
    private Integer paymentNumber;
    private BigDecimal initialBalance;
    private BigDecimal interestAmount;
    private BigDecimal principalAmount;
    private BigDecimal totalPayment;
    private BigDecimal endingBalance;
}
