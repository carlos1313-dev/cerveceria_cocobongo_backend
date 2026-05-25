package com.cocobongo.cerveceria.outgoings.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class BalanceReport {
          private BigDecimal gastos;
          private BigDecimal balance;

          private LocalDateTime fechaInicio;
          private LocalDateTime fechaFin;

}
