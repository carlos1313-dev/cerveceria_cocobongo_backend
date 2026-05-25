package com.cocobongo.cerveceria.sales.dto;

import java.util.List;

import com.cocobongo.cerveceria.sales.entities.PaymentType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * POST /api/v1/sales
 *
 * clientId es opcional — solo obligatorio cuando paymentType = CREDIT.
 * La validación de esa regla de negocio se hace en SaleService,
 * no aquí, porque @Valid no puede cruzar campos condicionalmente de forma limpia.
 */
@Data
public class RegisterSaleRequest {
 
    @NotNull(message = "El método de pago es obligatorio")
    private PaymentType paymentType;
 
    // Nullable — solo requerido si paymentType = CREDIT
    private Integer clientId;
 
    @NotEmpty(message = "La venta debe incluir al menos un producto")
    @Valid                   // propaga validación a cada SaleItemRequest
    private List<SaleItemRequest> items;
}