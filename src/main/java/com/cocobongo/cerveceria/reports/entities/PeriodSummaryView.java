package com.cocobongo.cerveceria.reports.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
 
import org.hibernate.annotations.Immutable;
 
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
 
/**
 * Entidad mapeada a la vista v_period_summary.
 * PK compuesta (sale_day, id_branch) — un registro por día y sucursal.
 *
 * CORRECCIÓN respecto a la versión anterior:
 *   La PK original usaba solo sale_day como @Id, lo que causaba que JPA
 *   colapsara filas de distintas sucursales en el mismo día.
 *   Se usa @EmbeddedId con (sale_day + id_branch) para representar
 *   correctamente la clave compuesta de la vista.
 */
@Entity
@Immutable
@Table(name = "v_period_summary")
@Getter
public class PeriodSummaryView {
 
    @EmbeddedId
    private PeriodSummaryId id;
 
    @Column(name = "branch_name")
    private String branchName;
 
    @Column(name = "total_sales")
    private Long totalSales;
 
    @Column(name = "gross_income")
    private BigDecimal grossIncome;
 
    @Column(name = "estimated_profit")
    private BigDecimal estimatedProfit;
 
    // ── PK compuesta ──────────────────────────────────────────────────────────
 
    @Embeddable
    @Getter
    public static class PeriodSummaryId implements Serializable {
 
        @Column(name = "sale_day")
        private LocalDate saleDay;
 
        @Column(name = "id_branch")
        private Integer idBranch;
 
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PeriodSummaryId)) return false;
            PeriodSummaryId that = (PeriodSummaryId) o;
            return Objects.equals(saleDay, that.saleDay)
                && Objects.equals(idBranch, that.idBranch);
        }
 
        @Override
        public int hashCode() {
            return Objects.hash(saleDay, idBranch);
        }
    }
}