package com.cocobongo.cerveceria.inventory.dto;

import java.math.BigDecimal;

import com.cocobongo.cerveceria.inventory.entities.ProductType;

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
public class ProductResponseDTO {

          private Integer idProduct;

          private Integer providerId;
          private String providerName;

          private String name;

          private String description;

          private ProductType type;

          private BigDecimal cost;

          private BigDecimal price;

          private Boolean isActive;
}