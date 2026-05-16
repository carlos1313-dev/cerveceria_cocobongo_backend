package com.cocobongo.cerveceria.clients.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InstallmentResponseDTO {

    private Long idInstallment;
    private Long idClient;
    private Long idUser;
    private Long idSale;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String notes;

}
