package com.cocobongo.cerveceria.exchangerate.dto;
 
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
import java.math.BigDecimal;
 
// ── Request ───────────────────────────────────────────────────────────────────
 
/** PATCH /api/v1/exchange-rate */
@Data
public class ExchangeRateRequest {
 
    @NotNull(message = "La tasa es obligatoria")
    @DecimalMin(value = "0.01", message = "La tasa debe ser mayor que cero")
    private BigDecimal rate;
}
 