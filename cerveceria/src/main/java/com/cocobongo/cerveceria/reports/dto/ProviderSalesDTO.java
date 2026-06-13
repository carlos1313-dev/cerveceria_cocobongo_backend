package com.cocobongo.cerveceria.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
// ── 3. Proveedor con más ventas ──────────────────────────────────────────────
 
/**
 * Ranking de proveedores según ingresos generados por sus productos
 * dentro del período solicitado.
 * Un proveedor "genera ventas" cuando sus productos aparecen en sale_detail.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class ProviderSalesDTO {
    private Integer    idProvider;
    private String     providerName;
    private Long       productsSold;    // unidades vendidas de todos sus productos
    private BigDecimal grossIncome;     // ingresos totales de sus productos
    private BigDecimal estimatedProfit; // utilidad estimada de sus productos
}