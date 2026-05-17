package com.cocobongo.cerveceria.reports.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopProductDTO {
 
    private Integer    idProduct;
    private String     productName;
    private Long       unitsSold;        // unidades vendidas en el período
    private BigDecimal totalRevenue;     // ingreso bruto del producto
    private BigDecimal estimatedProfit;  // ganancia estimada del producto
 
}