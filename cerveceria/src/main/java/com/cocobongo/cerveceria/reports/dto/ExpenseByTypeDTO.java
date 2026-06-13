package com.cocobongo.cerveceria.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Gasto total por tipo dentro del período — usado dentro de NetBalanceDTO.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class ExpenseByTypeDTO {
    private String     type;   // PERSONAL, MAINTENANCE, RENT, etc.
    private BigDecimal total;
}