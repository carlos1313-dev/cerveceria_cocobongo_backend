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
import com.cocobongo.cerveceria.reports.dto.DayOfWeekSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.DaySummaryDTO;
import com.cocobongo.cerveceria.reports.dto.ExpenseByTypeDTO;
import com.cocobongo.cerveceria.reports.dto.PeriodSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.ProviderSalesDTO;
import com.cocobongo.cerveceria.reports.dto.TopProductDTO;
import com.cocobongo.cerveceria.sales.entities.SaleEntity;
 
@Repository
public interface ReportRepository extends JpaRepository<SaleEntity, Integer> {
 
    // =========================================================================
    // QUERIES EXISTENTES — sin cambios
    // =========================================================================
 
    @Query(value = "SELECT DISTINCT s FROM SaleEntity s " +
                   "LEFT JOIN FETCH s.branch " +
                   "LEFT JOIN FETCH s.client " +
                   "LEFT JOIN FETCH s.user " +
                   "WHERE s.status = 'COMPLETED' " +
                   "AND s.saleDate >= :from AND s.saleDate <= :to " +
                   "ORDER BY s.saleDate DESC",
           countQuery = "SELECT COUNT(s) FROM SaleEntity s " +
                        "WHERE s.status = 'COMPLETED' " +
                        "AND s.saleDate >= :from AND s.saleDate <= :to")
    Page<SaleEntity> findByPeriod(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
                           Pageable      pageable);
 
    @Query(value = "SELECT DISTINCT s FROM SaleEntity s " +
                   "LEFT JOIN FETCH s.branch " +
                   "LEFT JOIN FETCH s.client " +
                   "LEFT JOIN FETCH s.user " +
                   "WHERE s.status = 'COMPLETED' " +
                   "AND s.saleDate >= :from AND s.saleDate <= :to " +
                   "AND s.branch.idBranch = :branchId " +
                   "ORDER BY s.saleDate DESC",
           countQuery = "SELECT COUNT(s) FROM SaleEntity s " +
                        "WHERE s.status = 'COMPLETED' " +
                        "AND s.saleDate >= :from AND s.saleDate <= :to " +
                        "AND s.branch.idBranch = :branchId")
    Page<SaleEntity> findByPeriodAndBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId,
                               Pageable      pageable);
 
    @Query("SELECT COALESCE(SUM(s.total), 0) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' AND s.saleDate >= :from AND s.saleDate <= :to")
    BigDecimal sumGrossIncome(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
 
    @Query("SELECT COALESCE(SUM(s.total), 0) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' AND s.saleDate >= :from AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId")
    BigDecimal sumGrossIncomeByBranch(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("branchId") Integer branchId);
 
    @Query("SELECT COUNT(s) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' AND s.saleDate >= :from AND s.saleDate <= :to")
    Long countSales(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
 
    @Query("SELECT COUNT(s) FROM SaleEntity s " +
           "WHERE s.status = 'COMPLETED' AND s.saleDate >= :from AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId")
    Long countSalesByBranch(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("branchId") Integer branchId);
 
    @Query("SELECT COALESCE(SUM((sd.unitPrice - sd.product.cost) * sd.quantity), 0) " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' AND s.saleDate >= :from AND s.saleDate <= :to")
    BigDecimal calculateEstimatedProfit(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
 
    @Query("SELECT COALESCE(SUM((sd.unitPrice - sd.product.cost) * sd.quantity), 0) " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' AND s.saleDate >= :from AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId")
    BigDecimal calculateEstimatedProfitByBranch(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("branchId") Integer branchId);
 
    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.TopProductDTO(" +
           "    sd.product.idProduct, sd.product.name, " +
           "    SUM(sd.quantity), SUM(sd.subtotal), " +
           "    SUM((sd.unitPrice - sd.product.cost) * sd.quantity)) " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' AND s.saleDate >= :from AND s.saleDate <= :to " +
           "GROUP BY sd.product.idProduct, sd.product.name " +
           "ORDER BY SUM(sd.quantity) DESC")
    List<TopProductDTO> findTopProducts(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);
 
    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.TopProductDTO(" +
           "    sd.product.idProduct, sd.product.name, " +
           "    SUM(sd.quantity), SUM(sd.subtotal), " +
           "    SUM((sd.unitPrice - sd.product.cost) * sd.quantity)) " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' AND s.saleDate >= :from AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId " +
           "GROUP BY sd.product.idProduct, sd.product.name " +
           "ORDER BY SUM(sd.quantity) DESC")
    List<TopProductDTO> findTopProductsByBranch(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("branchId") Integer branchId, Pageable pageable);
 
    @Query(value = "SELECT s.id_branch AS idBranch, b.name AS branchName, b.city AS city, " +
                   "COUNT(DISTINCT s.id_sale) AS totalSales, SUM(sd.subtotal) AS grossIncome, " +
                   "SUM((sd.unit_price - p.cost) * sd.quantity) AS estimatedProfit " +
                   "FROM sale s " +
                   "JOIN sale_detail sd ON sd.id_sale = s.id_sale " +
                   "JOIN product p ON p.id_product = sd.id_product " +
                   "JOIN branch b ON b.id_branch = s.id_branch " +
                   "WHERE s.status = 'COMPLETED' AND s.sale_date >= :from AND s.sale_date <= :to " +
                   "GROUP BY s.id_branch, b.name, b.city ORDER BY SUM(sd.subtotal) DESC",
           nativeQuery = true)
    List<BranchSalesReportDTO> findSalesByBranch(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
 
    @Query(value = "SELECT * FROM v_period_summary " +
                   "WHERE (:branchId IS NULL OR id_branch = :branchId) " +
                   "AND (CAST(:from AS date) IS NULL OR sale_day >= CAST(:from AS date)) " +
                   "AND (CAST(:to   AS date) IS NULL OR sale_day <= CAST(:to   AS date)) " +
                   "ORDER BY sale_day DESC",
           nativeQuery = true)
    List<PeriodSummaryDTO> findPeriodSummary(
            @Param("branchId") Integer branchId,
            @Param("from")     LocalDate from,
            @Param("to")       LocalDate to);
 
    // =========================================================================
    // NUEVAS QUERIES
    // =========================================================================
 
    // ── 1. Resumen diario para gráfica (agrupado por día) ─────────────────────
    // Retorna una fila por día: fecha + ventas + ingresos + utilidad.
    // Para gráficas de línea/barras en el dashboard y módulo de reportes.
 
    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.DaySummaryDTO(" +
           "    CAST(s.saleDate AS localdate), " +
           "    COUNT(s), " +
           "    COALESCE(SUM(s.total), 0), " +
           "    COALESCE(SUM((sd.unitPrice - sd.product.cost) * sd.quantity), 0)" +
           ") " +
           "FROM SaleEntity s JOIN s.details sd " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from AND s.saleDate <= :to " +
           "GROUP BY CAST(s.saleDate AS localdate) " +
           "ORDER BY CAST(s.saleDate AS localdate) ASC")
    List<DaySummaryDTO> findDailySummary(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);
 
    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.DaySummaryDTO(" +
           "    CAST(s.saleDate AS localdate), " +
           "    COUNT(s), " +
           "    COALESCE(SUM(s.total), 0), " +
           "    COALESCE(SUM((sd.unitPrice - sd.product.cost) * sd.quantity), 0)" +
           ") " +
           "FROM SaleEntity s JOIN s.details sd " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId " +
           "GROUP BY CAST(s.saleDate AS localdate) " +
           "ORDER BY CAST(s.saleDate AS localdate) ASC")
    List<DaySummaryDTO> findDailySummaryByBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);
 
    // ── 2. Día de la semana más activo ────────────────────────────────────────
    // EXTRACT(DOW FROM ...) → 0=domingo, 1=lunes, ..., 6=sábado (PostgreSQL).
    // Agrupa todas las ventas del período por día de la semana y suma ingresos.
    // El servicio añade el nombre del día (Lunes, Martes, etc.).
 
    @Query(value =
           "SELECT EXTRACT(DOW FROM s.sale_date)::int       AS dayOfWeek, " +
           "       COUNT(DISTINCT s.id_sale)                AS totalSales, " +
           "       COALESCE(SUM(s.total), 0)                AS grossIncome " +
           "FROM sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.sale_date >= :from AND s.sale_date <= :to " +
           "GROUP BY EXTRACT(DOW FROM s.sale_date) " +
           "ORDER BY SUM(s.total) DESC",
           nativeQuery = true)
    List<Object[]> findSalesByDayOfWeekRaw(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);
 
    @Query(value =
           "SELECT EXTRACT(DOW FROM s.sale_date)::int       AS dayOfWeek, " +
           "       COUNT(DISTINCT s.id_sale)                AS totalSales, " +
           "       COALESCE(SUM(s.total), 0)                AS grossIncome " +
           "FROM sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.sale_date >= :from AND s.sale_date <= :to " +
           "AND s.id_branch = :branchId " +
           "GROUP BY EXTRACT(DOW FROM s.sale_date) " +
           "ORDER BY SUM(s.total) DESC",
           nativeQuery = true)
    List<Object[]> findSalesByDayOfWeekByBranchRaw(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);
 
    // ── 3. Proveedor con más ventas ───────────────────────────────────────────
    // join sale_detail → product → provider, agrupando por proveedor.
    // Productos sin proveedor (type=MADE) quedan excluidos automáticamente
    // porque el JOIN excluye nulls por defecto.
 
    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.ProviderSalesDTO(" +
           "    sd.product.provider.idProvider, " +
           "    sd.product.provider.name, " +
           "    SUM(sd.quantity), " +
           "    COALESCE(SUM(sd.subtotal), 0), " +
           "    COALESCE(SUM((sd.unitPrice - sd.product.cost) * sd.quantity), 0)" +
           ") " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from AND s.saleDate <= :to " +
           "AND sd.product.provider IS NOT NULL " +
           "GROUP BY sd.product.provider.idProvider, sd.product.provider.name " +
           "ORDER BY SUM(sd.subtotal) DESC")
    List<ProviderSalesDTO> findSalesByProvider(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);
 
    @Query("SELECT NEW com.cocobongo.cerveceria.reports.dto.ProviderSalesDTO(" +
           "    sd.product.provider.idProvider, " +
           "    sd.product.provider.name, " +
           "    SUM(sd.quantity), " +
           "    COALESCE(SUM(sd.subtotal), 0), " +
           "    COALESCE(SUM((sd.unitPrice - sd.product.cost) * sd.quantity), 0)" +
           ") " +
           "FROM SaleDetailEntity sd JOIN sd.sale s " +
           "WHERE s.status = 'COMPLETED' " +
           "AND s.saleDate >= :from AND s.saleDate <= :to " +
           "AND s.branch.idBranch = :branchId " +
           "AND sd.product.provider IS NOT NULL " +
           "GROUP BY sd.product.provider.idProvider, sd.product.provider.name " +
           "ORDER BY SUM(sd.subtotal) DESC")
    List<ProviderSalesDTO> findSalesByProviderAndBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);
 
    // ── 4. Gastos por tipo para balance neto ──────────────────────────────────
    // Se usa junto con sumGrossIncome para calcular utilidadNeta = ingresos - gastos.
    // Retorna subtotales por tipo de gasto para el desglose en el dashboard.
 
    @Query(value =
           "SELECT o.type AS type, COALESCE(SUM(o.total), 0) AS total " +
           "FROM outgoing o " +
           "WHERE o.date >= CAST(:from AS date) AND o.date <= CAST(:to AS date) " +
           "GROUP BY o.type " +
           "ORDER BY SUM(o.total) DESC",
           nativeQuery = true)
    List<Object[]> findExpensesByTypeRaw(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);
 
    @Query(value =
           "SELECT o.type AS type, COALESCE(SUM(o.total), 0) AS total " +
           "FROM outgoing o " +
           "WHERE o.date >= CAST(:from AS date) AND o.date <= CAST(:to AS date) " +
           "AND o.id_branch = :branchId " +
           "GROUP BY o.type " +
           "ORDER BY SUM(o.total) DESC",
           nativeQuery = true)
    List<Object[]> findExpensesByTypeByBranchRaw(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);
 
    // Total de gastos (escalar) — para calcular utilidadNeta directamente
    @Query(value =
           "SELECT COALESCE(SUM(o.total), 0) FROM outgoing o " +
           "WHERE o.date >= CAST(:from AS date) AND o.date <= CAST(:to AS date)",
           nativeQuery = true)
    BigDecimal sumTotalExpenses(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);
 
    @Query(value =
           "SELECT COALESCE(SUM(o.total), 0) FROM outgoing o " +
           "WHERE o.date >= CAST(:from AS date) AND o.date <= CAST(:to AS date) " +
           "AND o.id_branch = :branchId",
           nativeQuery = true)
    BigDecimal sumTotalExpensesByBranch(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("branchId") Integer       branchId);
}