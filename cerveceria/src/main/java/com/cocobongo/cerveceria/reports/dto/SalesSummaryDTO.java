package com.cocobongo.cerveceria.reports.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesSummaryDTO {
 
    private Long       totalSales;       // número de ventas completadas
    private BigDecimal grossIncome;      // ingreso bruto
    private BigDecimal estimatedProfit;  // utilidad estimada
    private BigDecimal estimatedCost;    // costo estimado (grossIncome - estimatedProfit)
 
    private List<TopProductDTO> topProducts; // top 5 más vendidos por unidades
}
 