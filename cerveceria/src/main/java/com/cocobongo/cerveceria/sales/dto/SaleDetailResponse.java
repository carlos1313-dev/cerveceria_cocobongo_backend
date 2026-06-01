package com.cocobongo.cerveceria.sales.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Línea de detalle en la respuesta.
 */
@Data
public class SaleDetailResponse {
    private Integer    idSaleDetail;
    private Integer    productId;
    private String     productName;
    private Integer    quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}