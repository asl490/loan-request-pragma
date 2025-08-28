package com.pragma.bootcamp.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "\"RequestLoan\"")
@AllArgsConstructor()
@NoArgsConstructor
@Data
@Builder
public class RequestLoanEntity {
  private String id;
  private BigDecimal amount;
  private String currency;
  private Integer term;
  private String idUser;
  private Long idState;
  private Long idTypeLoan;
}

