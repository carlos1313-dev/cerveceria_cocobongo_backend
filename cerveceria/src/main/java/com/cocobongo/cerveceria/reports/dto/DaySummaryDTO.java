package com.cocobongo.cerveceria.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
// ── 1. Resumen por día (para gráfica de ventas en el tiempo) ─────────────────
 
/**
 * Un punto en la gráfica de ventas: (fecha, cantidad_ventas, ingresos, utilidad).
 * Se obtiene agrupando ventas por día dentro del período solicitado.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DaySummaryDTO {
    private LocalDate  saleDay;
    private Long       totalSales;      // número de ventas ese día
    private BigDecimal grossIncome;     // suma de totales en USD
    private BigDecimal estimatedProfit; // utilidad estimada del día
}