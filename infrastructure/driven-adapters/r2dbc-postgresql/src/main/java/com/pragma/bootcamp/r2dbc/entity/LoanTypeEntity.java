package com.pragma.bootcamp.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "\"TypeLoan\"")
@AllArgsConstructor()
@NoArgsConstructor
@Data
@Builder
public class LoanTypeEntity {
  @Id
  private Long id;
  private String name;
  private BigDecimal minAmount;
  private BigDecimal maxAmount;
  private String currency;
  private Double interestRate;
  private Boolean validationAutomatic;
}
