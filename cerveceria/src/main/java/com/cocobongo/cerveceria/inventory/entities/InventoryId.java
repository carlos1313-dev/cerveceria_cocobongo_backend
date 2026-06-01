package com.cocobongo.cerveceria.inventory.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
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
@NotNull(message = "El ID del producto es obligatorio")
	private Integer idProduct;

    @Column(name = "id_branch")
    @NotNull(message = "El ID de la sucursal es obligatorio")
    private Integer idBranch;
}