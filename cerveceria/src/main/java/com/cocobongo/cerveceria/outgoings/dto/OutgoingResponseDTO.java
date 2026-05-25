package com.cocobongo.cerveceria.outgoings.dto;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.outgoings.entities.OutgoingType;
import com.cocobongo.cerveceria.users.entities.UserEntity;

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

          private BranchEntity idBranch;

          private UserEntity idUser;

          private OutgoingType type;

          private LocalDateTime date;

          private BigDecimal total;

          private String description;
}