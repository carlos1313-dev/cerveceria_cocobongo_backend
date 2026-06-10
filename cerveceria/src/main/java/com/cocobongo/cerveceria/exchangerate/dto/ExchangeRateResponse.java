package com.cocobongo.cerveceria.exchangerate.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
// ── Response ──────────────────────────────────────────────────────────────────
 
/** Respuesta con la tasa actual — usada en el dashboard y en conversiones */
@Data
public class ExchangeRateResponse {
    private Integer       idRate;
    private BigDecimal    rate;
    private LocalDateTime registeredAt;
    private String        registeredBy;  // nombre del usuario que la actualizó
 
    // Campos calculados — útiles para el widget del dashboard
    // Ej: si rate = 567.68, estos muestran ejemplos de conversión
    private BigDecimal    oneDollarInVes;   // = rate (alias semántico)
    private BigDecimal    hundredVesInUsd;  // = 100 / rate
}