package com.cocobongo.cerveceria.sales.entities;

import com.cocobongo.cerveceria.inventory.entities.ProductEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Línea de detalle de una venta — un registro por cada producto vendido.
 *
 * unit_price es un snapshot del precio en el momento de la venta.
 * No referenciar product.price para cálculos históricos — puede haber cambiado.
 *
 * subtotal = quantity * unit_price
 * Se calcula en SaleService antes de persistir y se valida aquí vía @PrePersist.
 *
 * NOTA para el equipo: ProductEntity.idProduct debe ser Integer.
 * Si está declarado como Long en el módulo inventory, corregirlo para
 * mantener consistencia de tipos en todo el proyecto.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_detail")
public class SaleDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sale_detail")
    private Integer idSaleDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sale", nullable = false)
    private SaleEntity sale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_product", nullable = false)
    private ProductEntity product;

    @Column(name = "quantity", nullable = false)
    @Positive(message = "La cantidad debe ser positiva")
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @PositiveOrZero(message = "El precio unitario no puede ser negativo")
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    @PositiveOrZero(message = "El subtotal no puede ser negativo")
    private BigDecimal subtotal;

    /**
     * Recalcula y asigna el subtotal justo antes de persistir.
     * Actúa como última línea de defensa contra inconsistencias,
     * aunque el servicio ya lo calcula antes de llegar aquí.
     */
    @PrePersist
    protected void calculateSubtotal() {
        this.subtotal = this.unitPrice.multiply(
                BigDecimal.valueOf(this.quantity));
    }
}
