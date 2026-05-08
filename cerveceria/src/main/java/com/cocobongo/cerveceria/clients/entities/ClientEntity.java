package com.cocobongo.cerveceria.clients.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
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
@Table(name = "client")
public class ClientEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_client")
	private Long idClient;

	@Column(name = "name", nullable = false, length = 100)
	@NotBlank(message = "El nombre del cliente es obligatorio")
	private String name;

	@Column(name = "telephone", length = 20)
	private String telephone;

	@Column(name = "email", length = 100)
	@Email(message = "El formato del email no es válido")
	private String email;

	@Builder.Default
	@Column(name = "balance", nullable = false, precision = 10, scale = 2)
	@PositiveOrZero(message = "El saldo no puede ser negativo")
	private BigDecimal balance = BigDecimal.ZERO;

	@Builder.Default
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;
}
