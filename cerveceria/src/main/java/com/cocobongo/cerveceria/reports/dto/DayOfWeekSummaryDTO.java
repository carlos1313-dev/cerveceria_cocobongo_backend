package com.cocobongo.cerveceria.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
// ── 2. Día de la semana más activo ───────────────────────────────────────────
 
/**
 * Actividad agrupada por día de la semana en un período.
 * dayOfWeek: 0=domingo, 1=lunes, ..., 6=sábado (convención PostgreSQL DOW).
 * dayName:   "Domingo", "Lunes", etc. — calculado en el servicio.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DayOfWeekSummaryDTO {
    private Integer    dayOfWeek;       // 0–6
    private String     dayName;         // "Lunes", "Martes", etc.
    private Long       totalSales;      // ventas totales en ese día de la semana
    private BigDecimal grossIncome;     // ingresos totales en ese día de la semana
}