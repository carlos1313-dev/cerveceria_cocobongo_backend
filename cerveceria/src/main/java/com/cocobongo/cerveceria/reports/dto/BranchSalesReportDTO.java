package com.cocobongo.cerveceria.reports.dto;

import java.math.BigDecimal;

/**
 * Proyección para reporte de ventas agregadas por sucursal.
 * Incluye datos de la sucursal, cantidad de ventas e indicadores financieros.
 */

public interface BranchSalesReportDTO {
    Integer    getIdBranch();
    String     getBranchName();
    String     getCity();
    Long       getTotalSales();
    BigDecimal getGrossIncome();
    BigDecimal getEstimatedProfit();
}