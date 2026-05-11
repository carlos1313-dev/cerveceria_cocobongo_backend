package com.cocobongo.cerveceria.inventory.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "product")
public class ProductEntity {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_product")
	private Long idProduct;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_provider")
	private ProviderEntity provider;

	@Column(name = "name", nullable = false, length = 100)
	@NotBlank(message = "El nombre del producto es obligatorio")
	private String name;

	@Column(name = "description")
	private String description;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	@Column(name = "type", nullable = false, length = 10)
	private ProductType type = ProductType.RESALE;

	@Builder.Default
	@Column(name = "cost", nullable = false, precision = 10, scale = 2)
	@PositiveOrZero(message = "El costo no puede ser negativo")
	private BigDecimal cost = BigDecimal.ZERO;

	@Builder.Default
	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	@PositiveOrZero(message = "El precio no puede ser negativo")
	private BigDecimal price = BigDecimal.ZERO;

	@Builder.Default
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

}
