package com.cocobongo.cerveceria.inventory.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InventoryMovementRequestDTO {

    // ID del producto sobre el cual se realizará el movimiento de inventario
    @NotNull(message = "Id del producto es requerido")
    private Integer idProduct;

    // ID de la sucursal donde ocurre el movimiento
    @NotNull(message = "Id de la sucursal es requerido")
    private Integer idBranch;

    // ID del usuario que realiza el movimiento
    @NotNull(message = "Id del usuario es requerido")
    private Integer idUser;

    @NotNull(message = "Tipo es requerido")
    @Pattern(regexp = "IN|OUT", message = "Tipo debe ser 'IN' o 'OUT'")
    private String type;

    @NotNull(message = "Razón es requerida")
    @Pattern(regexp = "PURCHASE|ADJUSTMENT", message = "Razón debe ser 'PURCHASE' o 'ADJUSTMENT'")
    private String reason = "PURCHASE";

    // Cantidad de unidades a ingresar (debe ser mayor a 0)
    @NotNull(message = "Cantidad es requerida")
    @Positive(message = "Cantidad debe ser mayor a 0")
    private Integer quantity;

    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDateTime movementDate;

    private Integer idReference;
}