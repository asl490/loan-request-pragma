package com.pragma.bootcamp.r2dbc.entity;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "RequestLoan")
@AllArgsConstructor()
@NoArgsConstructor
@Data
@Builder
public class RequestLoanEntity {
  @Id
  private Long id;
  private BigDecimal amount;
  private String email;
  private String dni;
  private String currency;
  private Integer term;
  @Column("id_state")
  private Long requestStatus;
  @Column("id_loan_type")
  private Long loanType;
}
