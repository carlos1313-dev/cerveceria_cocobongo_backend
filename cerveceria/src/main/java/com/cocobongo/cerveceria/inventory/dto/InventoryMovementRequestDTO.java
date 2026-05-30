package com.cocobongo.cerveceria.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InventoryMovementRequestDTO {

    // ID del producto sobre el cual se realizará el movimiento de inventario
    @NotNull(message = "Id del producto es requerido")
    private Integer idProduct;

    // ID de la sucursal donde ocurre el movimiento
    @NotNull(message = "Id de la sucursal es requerido")
    private Integer idBranch;

    // Cantidad de unidades a ingresar (debe ser mayor a 0)
    @NotNull(message = "Cantidad es requerida")
    @Positive(message = "Cantidad debe ser mayor a 0")
    private Integer quantity;

    // Motivo del movimiento (solo PURCHASE o ADJUSTMENT)
    // Se valida aquí para evitar valores inválidos desde la entrada
    @Pattern(
        regexp = "PURCHASE|ADJUSTMENT",
        message = "Razon debe ser 'PURCHASE' o 'ADJUSTMENT'"
    )
    private String reason = "PURCHASE";

    // Observaciones opcionales del movimiento
    private String notes;
}