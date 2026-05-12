package com.cocobongo.cerveceria.branches.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "branch")
public class BranchEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_branch")
	private Integer idBranch;

	@Column(name = "name", nullable = false, length = 100)
	@NotBlank(message = "El nombre de la sucursal es obligatorio")
	private String name;

	@Column(name = "address")
	private String address;

	@Column(name = "city", length = 50)
	private String city;

	@Builder.Default
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;
}
