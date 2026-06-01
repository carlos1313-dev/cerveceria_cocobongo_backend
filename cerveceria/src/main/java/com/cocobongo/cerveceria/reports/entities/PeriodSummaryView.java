package com.cocobongo.cerveceria.reports.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad mapeada a la vista v_period_summary.
 * Representa un resumen agregado de ventas por día y sucursal.
 * Contiene el total de ventas, ingresos brutos y utilidad estimada para cada día y sucursal.
 * Es una entidad de solo lectura (inmutable) ya que se basa en una vista.
 */

@Entity
@Immutable
@Table(name = "v_period_summary")
public class PeriodSummaryView {

    @Id
    @Column(name = "sale_day")
    private LocalDate saleDay;

    @Column(name = "id_branch")
    private Integer idBranch;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "total_sales")
    private Long totalSales;

    @Column(name = "gross_income")
    private BigDecimal grossIncome;

    @Column(name = "estimated_profit")
    private BigDecimal estimatedProfit;
    
}
