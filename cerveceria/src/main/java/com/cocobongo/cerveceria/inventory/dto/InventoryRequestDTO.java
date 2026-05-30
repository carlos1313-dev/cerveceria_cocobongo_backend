package com.cocobongo.cerveceria.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestDTO {

    @NotNull
    private Integer idProduct;

    @NotNull
    private Integer idBranch;

    @NotNull
    @Positive(message = "Stock debe ser mayor a 0")
    private Integer stock;

    @NotNull
    @Positive(message = "Stock mínimo debe ser mayor a 0")
    private Integer minStock;
}
