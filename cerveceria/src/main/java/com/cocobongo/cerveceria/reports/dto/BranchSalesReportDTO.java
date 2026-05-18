package com.cocobongo.cerveceria.reports.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO para reporte de ventas agregadas por sucursal.
 * Incluye datos de la sucursal, cantidad de ventas e indicadores financieros.
 */
@Data
@AllArgsConstructor
public class BranchSalesReportDTO {
 
    private Integer    idBranch;
    private String     branchName;
    private String     city;
    private Long       totalSales;       // número de ventas
    private BigDecimal grossIncome;      // ingreso bruto
    private BigDecimal estimatedProfit;  // utilidad estimada

}