package com.cocobongo.cerveceria.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.cocobongo.cerveceria.sales.entities.PaymentType;
import com.cocobongo.cerveceria.sales.entities.SaleStatus;

import lombok.Data;

/**
 * Respuesta completa de una venta.
 * Siempre incluye ambas monedas para que el frontend alterne sin
 * llamadas adicionales al backend.
 */
@Data
public class SaleResponse {
    private Integer           idSale;
    private LocalDateTime     saleDate;
    private String            branchName;
    private String            registeredBy;
    private String            clientName;      // null si venta sin cliente
    private SaleStatus        status;
 
    // Totales en ambas monedas
    private BigDecimal        totalUsd;
    private BigDecimal        totalVes;        // totalUsd * rate de la venta
    private BigDecimal        exchangeRate;    // tasa BCV usada en esta venta

    private List<SaleDetailResponse>  details;
    private List<PaymentResponse>     payments;
}