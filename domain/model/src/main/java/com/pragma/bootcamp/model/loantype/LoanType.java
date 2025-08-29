package com.pragma.bootcamp.model.loantype;

import java.math.BigDecimal;

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
public class LoanType {
    private Long id;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String currency;
    private Double interestRate;
    private Boolean validationAutomatic;

    public boolean isAmountInRange(BigDecimal amount) {
        return amount != null &&
                minAmount != null &&
                maxAmount != null &&
                amount.compareTo(minAmount) >= 0 &&
                amount.compareTo(maxAmount) <= 0;
    }
}
