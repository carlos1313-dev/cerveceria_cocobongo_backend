package com.cocobongo.cerveceria.reports.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cocobongo.cerveceria.reports.dto.BranchSalesReportDTO;
import com.cocobongo.cerveceria.reports.dto.TopProductDTO;
import com.cocobongo.cerveceria.sales.entities.SaleEntity;

/**
 * Repositorio de consultas de reportes.
 * Contiene consultas específicas para obtener ventas por período, resumen de ventas,
 * utilidad estimada y top de productos.
 */
@Repository
public interface ReportRepository extends JpaRepository<SaleEntity, Integer> {

    // ── Ventas por período con paginación ─────────────────────────────────
    // GET /api/v1/reports/sales?from=&to=&branchId=

    @Query(value = "SELECT DISTINCT s FROM SaleEntity s " +
                   "LEFT JOIN FETCH s.branch " +
                   "LEFT JOIN FETCH s.client " +
                   "LEFT JOIN FETCH s.user " +
                   "WHERE s.status = 'COMPLETED' " +
                   "AND (:from     IS NULL OR s.saleDate   >= :from)     " +
                   "AND (:to       IS NULL OR s.saleDate   <= :to)       " +
                   "AND (:branchId IS NULL OR s.branch.idBranch = :branchId) " +
                   "ORDER BY s.saleDate DESC",
           countQuery = "SELECT COUNT(s) FROM SaleEntity s " +
                        "WHERE s.status = 'COMPLETED' " +
                        "AND (:from     IS NULL OR s.saleDate   >= :from) " +
                        "AND (:to       IS NULL OR s.saleDate   <= :to) " +
                        "AND (:branchId IS NULL OR s.branch.idBranch = :branchId)")
    Page<SaleEntity> findByPeriodAndBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId,
                               Pageable      pageable);
 
    // ── Total de ingresos brutos ──────────────────────────────────────────
    @Query("SELECT COALESCE(SUM(s.total), 0) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND (:from     IS NULL OR s.saleDate   >= :from)     " +
           "AND (:to       IS NULL OR s.saleDate   <= :to)       " +
           "AND (:branchId IS NULL OR s.branch.idBranch = :branchId)")
    BigDecimal sumGrossIncome(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);
 
    // ── Cantidad de ventas completadas ────────────────────────────────────
    @Query("SELECT COUNT(s) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND (:from     IS NULL OR s.saleDate   >= :from)     " +
           "AND (:to       IS NULL OR s.saleDate   <= :to)       " +
           "AND (:branchId IS NULL OR s.branch.idBranch = :branchId)")
    Long countSales(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);
 
    // ── Utilidad estimada ─────────────────────────────────────────────────
    // utilidadEstimada = SUM((unitPrice - product.cost) × quantity)
    // unitPrice = snapshot del precio en el momento de la venta
    // product.cost = costo actual del producto (por eso es "estimada")
    @Query("SELECT COALESCE(SUM((sd.unitPrice - sd.product.cost) * sd.quantity), 0) " +
           "FROM SaleDetailEntity sd " +
           "JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND (:from     IS NULL OR s.saleDate   >= :from)     " +
           "AND (:to       IS NULL OR s.saleDate   <= :to)       " +
           "AND (:branchId IS NULL OR s.branch.idBranch = :branchId)")
    BigDecimal calculateEstimatedProfit(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);
 
    // ── Top N productos más vendidos por cantidad ─────────────────────────
    // Se llama con Pageable.of(0, 5) para top 5
    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.TopProductDTO(" +
           "    sd.product.idProduct, " +
           "    sd.product.name, " +
           "    SUM(sd.quantity), " +
           "    SUM(sd.subtotal), " +
           "    SUM((sd.unitPrice - sd.product.cost) * sd.quantity)" +
           ") " +
           "FROM SaleDetailEntity sd " +
           "JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND (:from     IS NULL OR s.saleDate   >= :from)     " +
           "AND (:to       IS NULL OR s.saleDate   <= :to)       " +
           "AND (:branchId IS NULL OR s.branch.idBranch = :branchId) " +
           "GROUP BY sd.product.idProduct, sd.product.name " +
           "ORDER BY SUM(sd.quantity) DESC")
    List<TopProductDTO> findTopProducts(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId,
                               Pageable      pageable);
 
    // ── Ventas agrupadas por sucursal ─────────────────────────────────────
    // GET /api/v1/reports/sales/by-branch?period=
    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.BranchSalesReportDTO(" +
           "    s.branch.idBranch, " +
           "    s.branch.name, " +
           "    s.branch.city, " +
           "    COUNT(DISTINCT s.idSale), " +
           "    SUM(s.total), " +
           "    SUM((sd.unitPrice - sd.product.cost) * sd.quantity)" +
           ") " +
           "FROM SaleEntity s " +
           "JOIN s.details sd " +
           "WHERE s.status = 'COMPLETED' " +
           "AND (:from IS NULL OR s.saleDate >= :from) " +
           "AND (:to   IS NULL OR s.saleDate <= :to)   " +
           "GROUP BY s.branch.idBranch, s.branch.name, s.branch.city " +
           "ORDER BY SUM(s.total) DESC")
    List<BranchSalesReportDTO> findSalesByBranch(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);
}