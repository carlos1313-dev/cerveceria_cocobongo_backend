package com.cocobongo.cerveceria.sales.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Línea de detalle en la respuesta — con valores en ambas monedas.
 */
@Data
public class SaleDetailResponse {
    private Integer    idSaleDetail;
    private Integer    productId;
    private String     productName;
    private Integer    quantity;
 
    // Precio unitario y subtotal en ambas monedas
    private BigDecimal unitPriceUsd;
    private BigDecimal unitPriceVes;
    private BigDecimal subtotalUsd;
    private BigDecimal subtotalVes;
}