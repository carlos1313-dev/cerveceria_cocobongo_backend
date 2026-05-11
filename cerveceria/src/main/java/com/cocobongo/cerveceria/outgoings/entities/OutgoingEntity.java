package com.cocobongo.cerveceria.outgoings.entities;

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
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outgoing")
public class OutgoingEntity {

    public enum OutgoingType {
        PERSONAL,
        MAINTENANCE,
        RENT,
        SERVICES,
        EMPLOYEE,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_outgoing")
    private Long idOutgoing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_branch", nullable = false)
    private BranchEntity branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OutgoingType type;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    @Positive(message = "El total del egreso debe ser positivo")
    private BigDecimal total;

    @Column(name = "description")
    private String description;

    @PrePersist
    public void prePersist() {
        if (date == null) {
            date = LocalDate.now();
        }
    }
}
