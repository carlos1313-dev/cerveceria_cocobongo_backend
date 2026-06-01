package com.cocobongo.cerveceria.clients.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ClientRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String name;

    private String telephone;

    @Email(message = "El formato del email no es válido")
    private String email;

    @PositiveOrZero(message = "El saldo no puede ser negativo")
    private BigDecimal balance;

    private Boolean isActive;
}

