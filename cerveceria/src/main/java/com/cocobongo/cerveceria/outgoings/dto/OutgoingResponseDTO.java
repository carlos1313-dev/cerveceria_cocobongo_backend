package com.cocobongo.cerveceria.outgoings.dto;

import com.cocobongo.cerveceria.outgoings.entities.OutgoingType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutgoingResponseDTO {

          private Integer idOutgoing;

          private Integer idBranch;

          private Integer idUser;

          private OutgoingType type;

          private LocalDateTime date;

          private BigDecimal total;

          private String description;
}