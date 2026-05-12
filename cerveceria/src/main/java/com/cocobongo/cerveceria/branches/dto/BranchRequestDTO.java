package com.cocobongo.cerveceria.branches.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class BranchRequestDTO {
          private String address;
          private String city;
          private String name;
          private boolean isActive;
}
