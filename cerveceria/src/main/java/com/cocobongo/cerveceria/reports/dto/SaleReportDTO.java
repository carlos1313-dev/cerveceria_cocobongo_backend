package com.cocobongo.cerveceria.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.cocobongo.cerveceria.sales.entities.PaymentType;
import com.cocobongo.cerveceria.sales.entities.SaleStatus;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleReportDTO {
 
    private Integer       idSale;
    private LocalDateTime saleDate;
    private String        branchName;
    private String        registeredBy;
    private String        clientName;    // null si venta sin cliente
    private PaymentType   paymentType;
    private SaleStatus    status;
    private BigDecimal    total;

}
