package com.cocobongo.cerveceria.reports.controllers;
 
import java.time.LocalDateTime;
import java.util.List;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
import com.cocobongo.cerveceria.common.dto.ApiResponse;
import com.cocobongo.cerveceria.reports.dto.BranchSalesReportDTO;
import com.cocobongo.cerveceria.reports.dto.DayOfWeekSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.DaySummaryDTO;
import com.cocobongo.cerveceria.reports.dto.NetBalanceDTO;
import com.cocobongo.cerveceria.reports.dto.PeriodSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.ProviderSalesDTO;
import com.cocobongo.cerveceria.reports.dto.ReportPeriod;
import com.cocobongo.cerveceria.reports.dto.SaleReportDTO;
import com.cocobongo.cerveceria.reports.dto.SalesSummaryDTO;
import com.cocobongo.cerveceria.reports.services.ReportsService;
 
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
 
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Validated
public class ReportsController {
 
    private final ReportsService reportsService;
 
    // =========================================================================
    // ENDPOINTS EXISTENTES — sin cambios
    // =========================================================================
 
    /**
     * GET /api/v1/reports/sales?from=&to=&branchId=&page=&size=
     * RF-REP-01 — Lista paginada de ventas del período.
     */
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<Page<SaleReportDTO>>> getSalesByPeriod(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(defaultValue = "0")  @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
 
        return ResponseEntity.ok(ApiResponse.ok(
                reportsService.getSalesByPeriod(from, to, branchId, PageRequest.of(page, size))));
    }
 
    /**
     * GET /api/v1/reports/summary?from=&to=&branchId=
     * RF-REP-02, RF-REP-03 — Totales, utilidad estimada y top productos.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SalesSummaryDTO>> getSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer branchId) {
 
        return ResponseEntity.ok(ApiResponse.ok(
                reportsService.getSummary(from, to, branchId)));
    }
 
    /**
     * GET /api/v1/reports/sales/by-branch?period=
     * RF-REP-01 — Ventas agrupadas por sucursal.
     */
    @GetMapping("/sales/by-branch")
    public ResponseEntity<ApiResponse<List<BranchSalesReportDTO>>> getSalesByBranch(
            @RequestParam(defaultValue = "MONTHLY") ReportPeriod period) {
 
        return ResponseEntity.ok(ApiResponse.ok(
                reportsService.getSalesByBranch(period)));
    }
 
    /**
     * GET /api/v1/reports/period-summary?period=&branchId=
     * RF-REP-01 — Resumen desde la vista v_period_summary.
     */
    @GetMapping("/period-summary")
    public ResponseEntity<ApiResponse<List<PeriodSummaryDTO>>> getPeriodSummary(
            @RequestParam(defaultValue = "DAILY") ReportPeriod period,
            @RequestParam(required = false)        Integer       branchId) {
 
        return ResponseEntity.ok(ApiResponse.ok(
                reportsService.getPeriodSummary(period, branchId)));
    }
 
    // =========================================================================
    // NUEVOS ENDPOINTS
    // =========================================================================
 
    /**
     * GET /api/v1/reports/daily?from=&to=&branchId=
     *
     * Resumen día a día del período — base para gráficas de línea/barras.
     * Retorna: [{ saleDay, totalSales, grossIncome, estimatedProfit }, ...]
     * ordenado por fecha ASC (listo para pintar en el eje X).
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DaySummaryDTO>>> getDailySummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer branchId) {
 
        return ResponseEntity.ok(ApiResponse.ok(
                reportsService.getDailySummary(from, to, branchId)));
    }
 
    /**
     * GET /api/v1/reports/peak-day?from=&to=&branchId=
     *
     * Día con mayor volumen de ventas (en ingresos USD) dentro del período.
     * Útil para mostrar en el dashboard como dato destacado.
     */
    @GetMapping("/peak-day")
    public ResponseEntity<ApiResponse<DaySummaryDTO>> getPeakDay(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer branchId) {
 
        return ResponseEntity.ok(ApiResponse.ok(
                reportsService.getPeakDay(from, to, branchId)));
    }
 
    /**
     * GET /api/v1/reports/by-day-of-week?from=&to=&branchId=
     *
     * Actividad agrupada por día de la semana en el período.
     * Retorna los 7 días ordenados por ingresos DESC — el primero es el más activo.
     * Ejemplo: período junio → lunes sumados, martes sumados, ... etc.
     * dayOfWeek: 0=Domingo, 1=Lunes, ..., 6=Sábado
     */
    @GetMapping("/by-day-of-week")
    public ResponseEntity<ApiResponse<List<DayOfWeekSummaryDTO>>> getSalesByDayOfWeek(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer branchId) {
 
        return ResponseEntity.ok(ApiResponse.ok(
                reportsService.getSalesByDayOfWeek(from, to, branchId)));
    }
 
    /**
     * GET /api/v1/reports/by-provider?from=&to=&branchId=
     *
     * Ranking de proveedores según ingresos generados por sus productos.
     * Cada producto tiene un proveedor; se agrupa por proveedor y suma sus ventas.
     * Productos tipo MADE (sin proveedor) quedan excluidos.
     */
    @GetMapping("/by-provider")
    public ResponseEntity<ApiResponse<List<ProviderSalesDTO>>> getSalesByProvider(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer branchId) {
 
        return ResponseEntity.ok(ApiResponse.ok(
                reportsService.getSalesByProvider(from, to, branchId)));
    }
 
    /**
     * GET /api/v1/reports/balance?from=&to=&branchId=
     * RF-GAS-03 — Balance financiero neto del período.
     *
     * Retorna:
     *   grossIncome      = total ingresos por ventas (USD)
     *   estimatedProfit  = utilidad bruta estimada de ventas
     *   totalExpenses    = suma de gastos del mismo período
     *   netProfit        = grossIncome - totalExpenses
     *   grossMarginPct   = estimatedProfit / grossIncome * 100
     *   expensesByType   = desglose de gastos por tipo
     *
     * Con estos datos el frontend puede mostrar:
     *   - Gráfica ventas vs ganancias (grossIncome vs estimatedProfit)
     *   - Gráfica ingresos vs gastos  (grossIncome vs totalExpenses)
     *   - Utilidad neta del período
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<NetBalanceDTO>> getNetBalance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer branchId) {
 
        return ResponseEntity.ok(ApiResponse.ok(
                reportsService.getNetBalance(from, to, branchId)));
    }
}