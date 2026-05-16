package com.cocobongo.cerveceria.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.cocobongo.cerveceria.sales.entities.PaymentType;
import com.cocobongo.cerveceria.sales.entities.SaleStatus;

import lombok.Data;

/**
 * Respuesta completa de una venta registrada.
 * Sirve también como comprobante interno (GET /api/v1/sales/{id}/receipt).
 */
@Data
public class SaleResponse {
    private Integer       idSale;
    private LocalDateTime saleDate;
    private String        branchName;
    private String        registeredBy;   // nombre del usuario que registró
    private String        clientName;     // null si venta sin cliente
    private PaymentType   paymentType;
    private SaleStatus    status;
    private BigDecimal    total;
    private List<SaleDetailResponse> details;
}