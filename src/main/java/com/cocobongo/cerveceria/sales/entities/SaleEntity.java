package com.cocobongo.cerveceria.sales.entities;
 
import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.clients.entities.ClientEntity;
import com.cocobongo.cerveceria.exchangerate.entities.ExchangeRateEntity;
import com.cocobongo.cerveceria.users.entities.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Cabecera de una venta.
 *
 * Cambios respecto a la versión anterior:
 *   - Eliminado: paymentType (campo único) → reemplazado por List<SalePaymentEntity>
 *   - Añadido:   rate (tasa BCV vigente al momento de la venta)
 *   - Añadido:   payments (lista de pagos — cascade ALL)
 *
 * El status PENDING se determina en SalesService si algún pago es CREDIT.
 * total siempre en USD — es la suma de los subtotales de sale_detail.
 */
@Entity
@Table(name = "sale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sale")
    private Integer idSale;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_branch", nullable = false)
    private BranchEntity branch;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity user;
 
    // Nullable — solo obligatorio si algún pago es CREDIT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client")
    private ClientEntity client;
 
    // Tasa BCV vigente al momento de registrar la venta.
    // Se usa para mostrar valores históricos en bolívares de forma correcta.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rate")
    private ExchangeRateEntity rate;
 
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;
 
    // Total en USD — suma de subtotales de sale_detail
    @Builder.Default
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal total = BigDecimal.ZERO;
 
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 15)
    private SaleStatus status = SaleStatus.COMPLETED;
 
    // Detalles de productos vendidos
    @Builder.Default
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SaleDetailEntity> details = new ArrayList<>();
 
    // Pagos asociados a esta venta (uno o varios)
    @Builder.Default
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SalePaymentEntity> payments = new ArrayList<>();
 
    @PrePersist
    protected void onCreate() {
        this.saleDate = LocalDateTime.now(ZoneId.of("UTC"));
    }
 
    public void addDetail(SaleDetailEntity detail) {
        details.add(detail);
        detail.setSale(this);
    }
 
    public void addPayment(SalePaymentEntity payment) {
        payments.add(payment);
        payment.setSale(this);
    }
}