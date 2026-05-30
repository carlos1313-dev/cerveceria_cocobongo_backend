package com.cocobongo.cerveceria.branches.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchRequestDTO {
    private String address;
    private String city;
    private String name;

    @NotNull
    @JsonProperty("isActive")
    private Boolean isActive;
}