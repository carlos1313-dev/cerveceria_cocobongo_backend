package com.cocobongo.cerveceria.auth.entities;

import com.cocobongo.cerveceria.users.entities.User;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Table(name = "audit")
public class AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_audit")
    private Long idAudit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_session")
    private Session session;

    @Column(name = "action", nullable = false, length = 50)
    @NotBlank(message = "La acción es obligatoria")
    @Size(max = 50, message = "La acción no puede exceder 50 caracteres")
    private String action;

    @Column(name = "table_name", length = 50)
    @Size(max = 50, message = "El nombre de la tabla no puede exceder 50 caracteres")
    private String tableName;

    @Column(name = "id_record")
    private Long idRecord;

    @Column(name = "detail")
    private String detail;

    @Column(name = "ip_address", length = 45)
    @Size(max = 45, message = "La IP no puede exceder 45 caracteres")
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
