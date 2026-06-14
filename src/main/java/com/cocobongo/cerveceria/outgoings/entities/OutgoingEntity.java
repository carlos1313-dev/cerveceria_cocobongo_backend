package com.cocobongo.cerveceria.outgoings.entities;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.exchangerate.entities.ExchangeRateEntity;
import com.cocobongo.cerveceria.sales.entities.Currency;
import com.cocobongo.cerveceria.users.entities.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "outgoing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutgoingEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_outgoing")
    private Integer idOutgoing;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_branch", nullable = false)
    private BranchEntity idBranch;
 
    // Tomado del usuario autenticado — no viene en el request
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity idUser;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OutgoingType type;
 
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
 
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    @Positive(message = "El total del egreso debe ser positivo")
    private BigDecimal total;
 
    @Column(name = "description")
    private String description;
 
    // Moneda en que se realizó el gasto (USD por defecto)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "currency", nullable = false, length = 5)
    private Currency currency = Currency.USD;
 
    // Tasa BCV al momento del gasto — solo relevante si currency = VES
    // Permite mostrar el equivalente en USD en reportes históricos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rate")
    private ExchangeRateEntity rate;
 
    @PrePersist
    public void prePersist() {
        if (date == null) {
            date = LocalDateTime.now();
        }
    }
}