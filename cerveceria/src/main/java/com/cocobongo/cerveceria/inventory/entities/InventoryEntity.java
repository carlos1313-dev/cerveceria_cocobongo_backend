package com.cocobongo.cerveceria.inventory.entities;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory")
public class InventoryEntity {

	@EmbeddedId
	private InventoryId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("idProduct")
	@JoinColumn(name = "id_product", nullable = false)
	private ProductEntity product;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("idBranch")
	@JoinColumn(name = "id_branch", nullable = false)
	private BranchEntity branch;

	@Builder.Default
	@Column(name = "stock", nullable = false)
	@PositiveOrZero(message = "El stock no puede ser negativo")
	private Integer stock = 0;

	@Builder.Default
	@Column(name = "min_stock", nullable = false)
	@PositiveOrZero(message = "El stock mínimo no puede ser negativo")
	private Integer minStock = 0;
}
