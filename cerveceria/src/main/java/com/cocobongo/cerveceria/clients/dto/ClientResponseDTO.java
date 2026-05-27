package com.cocobongo.cerveceria.clients.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ClientResponseDTO {

    private Integer idClient;
    private String name;
    private String telephone;
    private String email;
    private BigDecimal balance;
    private Boolean isActive;

    public BigDecimal getAccountStatus() {
        return this.balance;
    }
}
