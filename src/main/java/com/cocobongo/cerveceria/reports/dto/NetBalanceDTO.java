package com.cocobongo.cerveceria.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
// ── 4. Balance neto (ingresos vs gastos) ─────────────────────────────────────
 
/**
 * Balance financiero del período:
 *   utilidadNeta = grossIncome - totalExpenses
 *   margenBruto  = estimatedProfit / grossIncome * 100
 *
 * Cubre el requisito de comparar ingresos vs gastos y ventas vs ganancias.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NetBalanceDTO {
    private BigDecimal grossIncome;       // suma sale.total (ventas completadas)
    private BigDecimal estimatedProfit;   // (unitPrice - cost) * qty — ganancia bruta
    private BigDecimal totalExpenses;     // suma outgoing.total del mismo período
    private BigDecimal netProfit;         // grossIncome - totalExpenses
    private BigDecimal grossMarginPct;    // estimatedProfit / grossIncome * 100
 
    // Desglose de gastos por tipo (para la mini-tabla del dashboard)
    private java.util.List<ExpenseByTypeDTO> expensesByType;
}
