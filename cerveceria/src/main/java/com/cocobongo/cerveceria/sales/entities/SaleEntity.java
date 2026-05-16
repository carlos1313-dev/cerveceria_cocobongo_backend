package com.cocobongo.cerveceria.sales.entities;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.clients.entities.ClientEntity;
import com.cocobongo.cerveceria.users.entities.UserEntity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Cabecera de una venta.
 *
 * Relaciones:
 *  - id_branch → BranchEntity  (NOT NULL — toda venta pertenece a una sucursal)
 *  - id_user   → UserEntity    (NOT NULL — quién registró la venta)
 *  - id_client → ClientEntity  (nullable — solo obligatorio si payment_type = CREDIT)
 *  - details   → SaleDetailEntity (cascade ALL — el detalle no existe sin la venta)
 *
 * La constraint chk_credit_requires_client se valida en la capa de servicio
 * antes de persistir (ver SaleService.registerSale).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale")
public class SaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sale")
    private Long idSale;

    // Sucursal donde se realizó la venta
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_branch", nullable = false)
    private BranchEntity branch;

    // Usuario que registró la venta (ADMIN o EMPLOYEE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity user;

    // Nullable — solo se asocia si el cliente es conocido o si el pago es CREDIT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client")
    private ClientEntity client;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    // Total calculado en el servicio — suma de subtotales de los detalles
    @Builder.Default
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero(message = "El total no puede ser negativo")
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 15)
    private SaleStatus status = SaleStatus.COMPLETED;



    // Cascade ALL: al guardar SaleEntity se persisten automáticamente los detalles.
    // OrphanRemoval: si se elimina un detalle de la lista, se borra de la BD.
    @Builder.Default
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SaleDetailEntity> details = new ArrayList<>();
 
    @PrePersist
    protected void onCreate() {
        this.saleDate = LocalDateTime.now();
    }
 
    // Helper para mantener la consistencia bidireccional de la relación
    public void addDetail(SaleDetailEntity detail) {
        details.add(detail);
        detail.setSale(this);
    }
}
