package com.cocobongo.cerveceria.outgoings.dto;

import com.cocobongo.cerveceria.outgoings.entities.OutgoingType;
import com.cocobongo.cerveceria.sales.entities.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutgoingRequestDTO {
 
    @NotNull(message = "La sucursal es obligatoria")
    private Integer idBranch;
 
    // idUser eliminado — se toma del usuario autenticado igual que en ventas
 
    @NotNull(message = "El tipo de egreso es obligatorio")
    private OutgoingType type;
 
    /**
     * Opcional: si llega null se asigna LocalDateTime.now() en @PrePersist
     */
    private LocalDateTime date;
 
    @NotNull(message = "El total es obligatorio")
    @Positive(message = "El total debe ser mayor que 0")
    private BigDecimal total;
 
    private String description;
 
    /**
     * Moneda del gasto — USD por defecto.
     * Si currency = VES, el servicio asocia la tasa BCV vigente
     * para conversiones históricas en reportes.
     */
    @Builder.Default
    private Currency currency = Currency.USD;
}
 