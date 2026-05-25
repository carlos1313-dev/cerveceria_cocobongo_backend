package com.cocobongo.cerveceria.clients.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InstallmentResponseDTO {

    private Integer idInstallment;
    private Integer idClient;
    private Integer idUser;
    private Integer idSale;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String notes;

}
