package com.cocobongo.cerveceria.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.cocobongo.cerveceria.sales.entities.PaymentType;
import com.cocobongo.cerveceria.sales.entities.SaleStatus;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO para reporte de cada venta individual.
 * Se usa en la paginación de ventas por período.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleReportDTO {
 
    private Integer       idSale;          // identificador de la venta
    private LocalDateTime saleDate;        // fecha y hora de la venta
    private String        branchName;      // sucursal donde se realizó la venta
    private String        registeredBy;    // usuario que registró la venta
    private String        clientName;      // nombre del cliente, puede ser null si es venta al público
    private PaymentType   paymentType;     // forma de pago utilizada
    private SaleStatus    status;          // estado de la venta
    private BigDecimal    total;           // total facturado de la venta

}
