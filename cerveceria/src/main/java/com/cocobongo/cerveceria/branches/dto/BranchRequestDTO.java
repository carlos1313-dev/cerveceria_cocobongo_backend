package com.cocobongo.cerveceria.branches.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class BranchRequestDTO {
          private String address;
          private String city;
          private String name;
          @NotNull
          private boolean isActive;
}
