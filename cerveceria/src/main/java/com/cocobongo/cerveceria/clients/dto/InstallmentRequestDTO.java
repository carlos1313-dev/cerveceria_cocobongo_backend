package com.cocobongo.cerveceria.clients.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InstallmentRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Integer idClient;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer idUser;

    private Integer idSale;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDateTime paymentDate;

    private String notes;

}
