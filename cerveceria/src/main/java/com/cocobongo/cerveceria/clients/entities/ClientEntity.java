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

/**
 * Cliente de la cervecería (quien compra).
 * Distinto de UserEntity (quien opera el sistema).
 *
 * El campo balance es calculado — nunca actualizar con UPDATE directo.
 * Se mantiene desde la capa de aplicación al registrar ventas a crédito
 * o abonos (installments).
 *
 * balance = SUM(sale.total WHERE payment_type = CREDIT AND id_client = this)
 *         - SUM(installment.amount WHERE id_client = this)
 */
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
	private Integer idClient;

	@Column(name = "name", nullable = false, length = 100)
	@NotBlank(message = "El nombre del cliente es obligatorio")
	private String name;

	@Column(name = "telephone", length = 20)
	private String telephone;

	@Column(name = "email", length = 100)
	@Email(message = "El formato del email no es válido")
	private String email;

	/**
     * Saldo pendiente de pago.
     * NUNCA modificar con setBalance() directamente desde fuera del servicio.
     * Usar SaleService.registerSale() e InstallmentService.registerInstallment()
     * como únicos puntos de escritura.
     */
	@Builder.Default
	@Column(name = "balance", nullable = false, precision = 10, scale = 2)
	@PositiveOrZero(message = "El saldo no puede ser negativo")
	private BigDecimal balance = BigDecimal.ZERO;

	@Builder.Default
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;
}
