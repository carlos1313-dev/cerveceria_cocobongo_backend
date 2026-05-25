package com.cocobongo.cerveceria.outgoings.dto;


import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.outgoings.entities.OutgoingType;
import com.cocobongo.cerveceria.users.entities.UserEntity;

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
    private BranchEntity idBranch;

    @NotNull(message = "El usuario es obligatorio")
    private UserEntity idUser;

    @NotNull(message = "El tipo de egreso es obligatorio")
    private OutgoingType type;

    /**
     * Opcional:
     * si llega null, la entidad ya asigna LocalDateTime.now() con @PrePersist
     */
    private LocalDateTime date;

    @NotNull(message = "El total es obligatorio")
    @Positive(message = "El total debe ser mayor que 0")
    private BigDecimal total;

    private String description;
}
