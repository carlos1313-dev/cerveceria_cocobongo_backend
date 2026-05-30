package com.cocobongo.cerveceria.inventory.dto;

import java.math.BigDecimal;

import com.cocobongo.cerveceria.branches.dto.BranchRequestDTO;
import com.cocobongo.cerveceria.inventory.entities.ProductType;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private Integer providerId;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    private String description;

    @Builder.Default
    private ProductType type = ProductType.RESALE;

    @Builder.Default
    @PositiveOrZero(message = "El costo no puede ser negativo")
    private BigDecimal cost = BigDecimal.ZERO;

    @Builder.Default
    @PositiveOrZero(message = "El precio no puede ser negativo")
    private BigDecimal price = BigDecimal.ZERO;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("branch")
    private BranchRequestDTO branch;

    private Integer initialStock;
    private Integer minStock;
}