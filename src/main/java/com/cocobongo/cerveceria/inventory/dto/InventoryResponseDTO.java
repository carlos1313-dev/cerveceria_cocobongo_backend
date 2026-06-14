package com.cocobongo.cerveceria.inventory.dto;

import java.math.BigDecimal;

import com.cocobongo.cerveceria.inventory.entities.ProductType;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponseDTO {

    // Identificadores del producto y sucursal
    private Integer idProduct;
    private Integer idBranch;
    private Integer idProvider;


    // Stock actual y stock mínimo configurado
    private Integer stock;
    private Integer minStock;

    // Datos adicionales del producto (para respuesta)
    private String productName;
    private ProductType productType;
    private BigDecimal price;
    private BigDecimal cost;

}