package com.cocobongo.cerveceria.reports.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * DTO para el resumen de ventas.
 * Contiene totales, indicadores financieros y los productos más vendidos.
 */
@Data
@Builder
public class SalesSummaryDTO {
 
    private Long       totalSales;       // número de ventas completadas
    private BigDecimal grossIncome;      // ingreso bruto total en el período
    private BigDecimal estimatedProfit;  // utilidad estimada del período
    private BigDecimal estimatedCost;    // costo estimado de lo vendido
 
    private List<TopProductDTO> topProducts; // top 5 más vendidos por unidades
}
 