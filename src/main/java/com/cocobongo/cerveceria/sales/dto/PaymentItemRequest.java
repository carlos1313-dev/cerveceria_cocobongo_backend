package com.cocobongo.cerveceria.sales.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;
import com.cocobongo.cerveceria.sales.entities.Currency;
import com.cocobongo.cerveceria.sales.entities.PaymentType;
/**
 * Cada pago dentro de RegisterSaleRequest.
 *
 * amount   = monto en la moneda indicada por currency
 *            Si currency=VES, el usuario ingresa el monto en bolívares.
 *            Si currency=USD, el usuario ingresa el monto en dólares.
 * currency = USD o VES
 * method   = CASH, BCV, BINANCE, BANCRECER, CREDIT
 */
@Data
public class PaymentItemRequest {
 
    @NotNull(message = "El método de pago es obligatorio")
    private PaymentType method;
 
    @NotNull(message = "La moneda es obligatoria")
    private Currency currency;
 
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
    private BigDecimal amount;
}