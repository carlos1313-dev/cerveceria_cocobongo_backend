package com.cocobongo.cerveceria.branches.dto;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;

import lombok.Data;

@Data
public class BranchResponseDTO {

          private Integer id;
          private String name;
          private String address;
          private String city;
          private Boolean is_active;

          public BranchResponseDTO(BranchEntity b ){
                    this.id = b.getIdBranch();
                    this.name = b.getName();
                    this.address = b.getAddress();
                    this.city = b.getCity();
                    this.is_active = b.getIsActive();
          }

}
