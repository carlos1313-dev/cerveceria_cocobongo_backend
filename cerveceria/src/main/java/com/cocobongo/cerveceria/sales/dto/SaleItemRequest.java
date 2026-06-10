package com.cocobongo.cerveceria.sales.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Cada línea de producto dentro de RegisterSaleRequest.
 */
@Data
public class SaleItemRequest {
 
    @NotNull(message = "El id del producto es obligatorio")
    private Integer productId;
 
    @NotNull(message = "La sucursal del producto es obligatoria")
    private Integer branchId;
 
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;
}