package com.cocobongo.cerveceria.sales.dto;

import java.util.List;

import com.cocobongo.cerveceria.sales.entities.PaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.cocobongo.cerveceria.sales.entities.Currency;
import com.cocobongo.cerveceria.sales.entities.SaleStatus;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * POST /api/v1/sales
 *
 * Una venta puede tener uno o más pagos simultáneos.
 * clientId es obligatorio si algún pago tiene method = CREDIT.
 * La validación cruzada se hace en SalesService.
 */
@Data
public class RegisterSaleRequest {
 
    // Nullable — obligatorio solo si algún pago es CREDIT
    private Integer clientId;
 
    @NotEmpty(message = "La venta debe incluir al menos un producto")
    @Valid
    private List<SaleItemRequest> items;
 
    @NotEmpty(message = "La venta debe incluir al menos un método de pago")
    @Valid
    private List<PaymentItemRequest> payments;
}