package com.cocobongo.cerveceria.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para resumen de ventas por período y sucursal.
 * Contiene datos agregados de ventas, ingresos y utilidad estimada para cada día y sucursal.
 */

public interface PeriodSummaryDTO {
    Integer   getIdBranch();
    String    getBranchName();
    LocalDate getSaleDay();
    Long      getTotalSales();
    BigDecimal getGrossIncome();
    BigDecimal getEstimatedProfit();
}