package com.cocobongo.cerveceria.inventory.entities;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.users.entities.UserEntity;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory_movement")
public class InventoryMovementEntity {

	public enum MovementType {
		IN,
		OUT
	}

	public enum MovementReason {
		PURCHASE,
		SALE,
		PRODUCTION,
		TRANSFER,
		ADJUSTMENT,
		RETURN
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_movement")
	private Long idMovement;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_product", nullable = false)
	private ProductEntity product;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_branch", nullable = false)
	private BranchEntity branch;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_user", nullable = false)
	private UserEntity user;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 10)
	private MovementType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "reason", nullable = false, length = 15)
	private MovementReason reason;

	@Column(name = "quantity", nullable = false)
	@NotNull(message = "La cantidad es obligatoria")
	@Positive(message = "La cantidad debe ser positiva")
	private Integer quantity;

	@Column(name = "movement_date", nullable = false)
	private LocalDateTime movementDate;

	@Column(name = "id_reference")
	private Long idReference;

	@PrePersist
	public void prePersist() {
		if (movementDate == null) {
			movementDate = LocalDateTime.now();
		}
	}
}
