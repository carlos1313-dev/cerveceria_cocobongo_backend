package com.cocobongo.cerveceria.outgoings.dto;

import com.cocobongo.cerveceria.outgoings.entities.OutgoingType;
import com.cocobongo.cerveceria.sales.entities.Currency;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutgoingResponseDTO {
 
    private Integer       idOutgoing;
    private Integer       idBranch;
    private Integer       idUser;
    private OutgoingType  type;
    private LocalDateTime date;
    private String        description;
    private Currency      currency;
 
    // Monto en la moneda original del gasto
    private BigDecimal total;
 
    // Equivalente en USD — calculado si currency=VES usando la tasa del momento
    // Si currency=USD, totalUsd = total
    private BigDecimal totalUsd;
 
    // Equivalente en VES — calculado si currency=USD usando la tasa del momento
    // Si currency=VES, totalVes = total
    private BigDecimal totalVes;
 
    // Tasa BCV usada para los cálculos
    private BigDecimal exchangeRate;
}