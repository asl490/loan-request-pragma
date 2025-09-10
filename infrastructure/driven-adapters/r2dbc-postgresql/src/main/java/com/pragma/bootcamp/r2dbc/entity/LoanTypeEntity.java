package com.pragma.bootcamp.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "typeloan")
@AllArgsConstructor()
@NoArgsConstructor
@Data
@Builder
public class LoanTypeEntity {
    @Id
    private Long id;
    private String name;
    @Column("min_amount")
    private BigDecimal minAmount;
    @Column("max_amount")
    private BigDecimal maxAmount;
    private String currency;
    @Column("interest_rate")
    private Double interestRate;
    @Column("validation_automatic")
    private Boolean validationAutomatic;
}
