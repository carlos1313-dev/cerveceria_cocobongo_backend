package com.cocobongo.cerveceria.inventory.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class InventoryId implements Serializable {

    @Column(name = "id_product")
    private Long idProduct;

    @Column(name = "id_branch")
    private Long idBranch;
}