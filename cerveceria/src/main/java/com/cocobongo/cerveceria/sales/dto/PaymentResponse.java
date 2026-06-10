package com.cocobongo.cerveceria.sales.dto;
import java.math.BigDecimal;
import com.cocobongo.cerveceria.sales.entities.Currency;
import com.cocobongo.cerveceria.sales.entities.PaymentType;
import lombok.Data;

/**
 * Pago individual en la respuesta.
 */
@Data
public class PaymentResponse {
    private Integer     idPayment;
    private PaymentType method;
    private Currency    currency;
    private BigDecimal  amount;      // en la moneda original
    private BigDecimal  amountUsd;   // equivalente en USD
}