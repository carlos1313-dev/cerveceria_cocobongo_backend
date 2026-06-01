package com.cocobongo.cerveceria.reports.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cocobongo.cerveceria.reports.dto.BranchSalesReportDTO;
import com.cocobongo.cerveceria.reports.dto.PeriodSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.TopProductDTO;
import com.cocobongo.cerveceria.sales.entities.SaleEntity;

/**
 * Repositorio de consultas de reportes.
 * Contiene consultas específicas para obtener ventas por período, resumen de ventas,
 * utilidad estimada y top de productos.
 */
@Repository
public interface ReportRepository extends JpaRepository<SaleEntity, Integer> {

    // ── Ventas por período sin filtro de sucursal ─────────────────────────
    @Query(value = "SELECT DISTINCT s FROM SaleEntity s " +
                   "LEFT JOIN FETCH s.branch " +
                   "LEFT JOIN FETCH s.client " +
                   "LEFT JOIN FETCH s.user " +
                   "WHERE s.status = 'COMPLETED' " +
                   "AND s.saleDate >= :from " +
                   "AND s.saleDate <= :to " +
                   "ORDER BY s.saleDate DESC",
           countQuery = "SELECT COUNT(s) FROM SaleEntity s " +
                        "WHERE s.status = 'COMPLETED' " +
                        "AND s.saleDate >= :from " +
                        "AND s.saleDate <= :to")
    Page<SaleEntity> findByPeriod(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
                           Pageable      pageable);

    // ── Ventas por período filtradas por sucursal ──────────────────────────
    @Query(value = "SELECT DISTINCT s FROM SaleEntity s " +
                   "LEFT JOIN FETCH s.branch " +
                   "LEFT JOIN FETCH s.client " +
                   "LEFT JOIN FETCH s.user " +
                   "WHERE s.status = 'COMPLETED' " +
                   "AND s.saleDate >= :from " +
                   "AND s.saleDate <= :to " +
                   "AND s.branch.idBranch = :branchId " +
                   "ORDER BY s.saleDate DESC",
           countQuery = "SELECT COUNT(s) FROM SaleEntity s " +
                        "WHERE s.status = 'COMPLETED' " +
                        "AND s.saleDate >= :from " +
                        "AND s.saleDate <= :to " +
                        "AND s.branch.idBranch = :branchId")
    Page<SaleEntity> findByPeriodAndBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId,
                               Pageable      pageable);

    // ── Total de ingresos brutos ──────────────────────────────────────────
    @Query("SELECT COALESCE(SUM(s.total), 0) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from " +
           "AND s.saleDate <= :to")
    BigDecimal sumGrossIncome(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    @Query("SELECT COALESCE(SUM(s.total), 0) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from " +
           "AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId")
    BigDecimal sumGrossIncomeByBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);

    // ── Cantidad de ventas completadas ────────────────────────────────────
    @Query("SELECT COUNT(s) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from " +
           "AND s.saleDate <= :to")
    Long countSales(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    @Query("SELECT COUNT(s) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from " +
           "AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId")
    Long countSalesByBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);

    // ── Utilidad estimada ─────────────────────────────────────────────────
    @Query("SELECT COALESCE(SUM((sd.unitPrice - sd.product.cost) * sd.quantity), 0) " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from " +
           "AND s.saleDate <= :to")
    BigDecimal calculateEstimatedProfit(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    @Query("SELECT COALESCE(SUM((sd.unitPrice - sd.product.cost) * sd.quantity), 0) " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from " +
           "AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId")
    BigDecimal calculateEstimatedProfitByBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);

    // ── Top N productos más vendidos ──────────────────────────────────────
    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.TopProductDTO(" +
           "    sd.product.idProduct, sd.product.name, " +
           "    SUM(sd.quantity), SUM(sd.subtotal), " +
           "    SUM((sd.unitPrice - sd.product.cost) * sd.quantity)" +
           ") " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from " +
           "AND s.saleDate <= :to " +
           "GROUP BY sd.product.idProduct, sd.product.name " +
           "ORDER BY SUM(sd.quantity) DESC")
    List<TopProductDTO> findTopProducts(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
                           Pageable      pageable);

    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.TopProductDTO(" +
           "    sd.product.idProduct, sd.product.name, " +
           "    SUM(sd.quantity), SUM(sd.subtotal), " +
           "    SUM((sd.unitPrice - sd.product.cost) * sd.quantity)" +
           ") " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from " +
           "AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId " +
           "GROUP BY sd.product.idProduct, sd.product.name " +
           "ORDER BY SUM(sd.quantity) DESC")
    List<TopProductDTO> findTopProductsByBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId,
                               Pageable      pageable);

    // ── Ventas agrupadas por sucursal ─────────────────────────────────────
    @Query(value = "SELECT " +
                   "    s.id_branch                                      AS idBranch, " +
                   "    b.name                                           AS branchName, " +
                   "    b.city                                           AS city, " +
                   "    COUNT(DISTINCT s.id_sale)                        AS totalSales, " +
                   "    SUM(sd.subtotal)                                 AS grossIncome, " +
                   "    SUM((sd.unit_price - p.cost) * sd.quantity)      AS estimatedProfit " +
                   "FROM sale s " +
                   "JOIN sale_detail sd ON sd.id_sale   = s.id_sale " +
                   "JOIN product     p  ON p.id_product = sd.id_product " +
                   "JOIN branch      b  ON b.id_branch  = s.id_branch " +
                   "WHERE s.status = 'COMPLETED' " +
                   "AND s.sale_date >= :from " +
                   "AND s.sale_date <= :to " +
                   "GROUP BY s.id_branch, b.name, b.city " +
                   "ORDER BY SUM(sd.subtotal) DESC",
           nativeQuery = true)
    List<BranchSalesReportDTO> findSalesByBranch(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    // ── Vista integrada: resumen por período y sucursal ───────────────────
    @Query(value = "SELECT * FROM v_period_summary " +
                   "WHERE (:branchId IS NULL OR id_branch = :branchId) " +
                   "AND   (CAST(:from AS date) IS NULL OR sale_day >= CAST(:from AS date)) " +
                   "AND   (CAST(:to   AS date) IS NULL OR sale_day <= CAST(:to   AS date)) " +
                   "ORDER BY sale_day DESC",
           nativeQuery = true)
    List<PeriodSummaryDTO> findPeriodSummary(
            @Param("branchId") Integer   branchId,
            @Param("from")     LocalDate from,
            @Param("to")       LocalDate to);
}