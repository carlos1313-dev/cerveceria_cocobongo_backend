package com.cocobongo.cerveceria.reports.services;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import com.cocobongo.cerveceria.reports.dto.BranchSalesReportDTO;
import com.cocobongo.cerveceria.reports.dto.DayOfWeekSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.DaySummaryDTO;
import com.cocobongo.cerveceria.reports.dto.ExpenseByTypeDTO;
import com.cocobongo.cerveceria.reports.dto.NetBalanceDTO;
import com.cocobongo.cerveceria.reports.dto.PeriodSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.ProviderSalesDTO;
import com.cocobongo.cerveceria.reports.dto.ReportPeriod;
import com.cocobongo.cerveceria.reports.dto.SaleReportDTO;
import com.cocobongo.cerveceria.reports.dto.SalesSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.TopProductDTO;
import com.cocobongo.cerveceria.reports.repositories.ReportRepository;
import com.cocobongo.cerveceria.sales.entities.SaleEntity;
 
import lombok.RequiredArgsConstructor;
 
@RequiredArgsConstructor
@Service
public class ReportsService {
 
    private final ReportRepository reportRepository;
 
    private static final LocalDateTime MIN_DATE = LocalDateTime.of(2000,  1,  1,  0,  0,  0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(2100, 12, 31, 23, 59, 59);
 
    // Nombres de días de la semana (índice = DOW de PostgreSQL: 0=domingo)
    private static final Map<Integer, String> DAY_NAMES = Map.of(
        0, "Domingo", 1, "Lunes", 2, "Martes", 3, "Miércoles",
        4, "Jueves",  5, "Viernes", 6, "Sábado"
    );
 
    private LocalDateTime safeFrom(LocalDateTime from) { return from != null ? from : MIN_DATE; }
    private LocalDateTime safeTo  (LocalDateTime to)   { return to   != null ? to   : MAX_DATE; }
 
    // =========================================================================
    // MÉTODOS EXISTENTES — sin cambios de firma
    // =========================================================================
 
    @Transactional(readOnly = true)
    public Page<SaleReportDTO> getSalesByPeriod(LocalDateTime from, LocalDateTime to,
                                                Integer branchId, Pageable pageable) {
        LocalDateTime f = safeFrom(from);
        LocalDateTime t = safeTo(to);
 
        Page<SaleEntity> page = branchId == null
                ? reportRepository.findByPeriod(f, t, pageable)
                : reportRepository.findByPeriodAndBranch(f, t, branchId, pageable);
 
        return page.map(sale -> SaleReportDTO.builder()
                .idSale(sale.getIdSale())
                .saleDate(sale.getSaleDate())
                .total(sale.getTotal())
                .branchName(sale.getBranch() != null ? sale.getBranch().getName() : null)
                .clientName(sale.getClient() != null ? sale.getClient().getName() : null)
                .registeredBy(sale.getUser() != null ? sale.getUser().getName()   : null)
                .status(sale.getStatus())
                .build());
    }
 
    @Transactional(readOnly = true)
    public SalesSummaryDTO getSummary(LocalDateTime from, LocalDateTime to, Integer branchId) {
        LocalDateTime f = safeFrom(from);
        LocalDateTime t = safeTo(to);
 
        Long       totalSales      = branchId == null
                ? reportRepository.countSales(f, t)
                : reportRepository.countSalesByBranch(f, t, branchId);
 
        BigDecimal grossIncome     = branchId == null
                ? reportRepository.sumGrossIncome(f, t)
                : reportRepository.sumGrossIncomeByBranch(f, t, branchId);
 
        BigDecimal estimatedProfit = branchId == null
                ? reportRepository.calculateEstimatedProfit(f, t)
                : reportRepository.calculateEstimatedProfitByBranch(f, t, branchId);
 
        BigDecimal estimatedCost   = grossIncome.subtract(estimatedProfit);
 
        List<TopProductDTO> topProducts = branchId == null
                ? reportRepository.findTopProducts(f, t, PageRequest.of(0, 5))
                : reportRepository.findTopProductsByBranch(f, t, branchId, PageRequest.of(0, 5));
 
        return SalesSummaryDTO.builder()
                .totalSales(totalSales)
                .grossIncome(grossIncome)
                .estimatedProfit(estimatedProfit)
                .estimatedCost(estimatedCost)
                .topProducts(topProducts)
                .build();
    }
 
    @Transactional(readOnly = true)
    public List<BranchSalesReportDTO> getSalesByBranch(ReportPeriod period) {
        LocalDateTime[] range = resolvePeriod(period);
        return reportRepository.findSalesByBranch(range[0], range[1]);
    }
 
    @Transactional(readOnly = true)
    public List<PeriodSummaryDTO> getPeriodSummary(ReportPeriod period, Integer branchId) {
        LocalDate[] range = resolvePeriodAsDate(period);
        return reportRepository.findPeriodSummary(branchId, range[0], range[1]);
    }
 
    // =========================================================================
    // NUEVOS MÉTODOS
    // =========================================================================
 
    // ── 1. Resumen diario para gráficas ──────────────────────────────────────
    // Retorna lista de (día, ventas, ingresos, utilidad) ordenada por fecha ASC.
    // El frontend la usa directamente para gráficas de línea o barras.
 
    @Transactional(readOnly = true)
    public List<DaySummaryDTO> getDailySummary(LocalDateTime from, LocalDateTime to,
                                                Integer branchId) {
        LocalDateTime f = safeFrom(from);
        LocalDateTime t = safeTo(to);
        return branchId == null
                ? reportRepository.findDailySummary(f, t)
                : reportRepository.findDailySummaryByBranch(f, t, branchId);
    }
 
    // ── 2. Día con más ventas del período ─────────────────────────────────────
    // Deriva del resumen diario — el primer elemento ya es el día con más ingresos
    // si la query ordena DESC. Aquí tomamos el máximo del resultado getDailySummary.
 
    @Transactional(readOnly = true)
    public DaySummaryDTO getPeakDay(LocalDateTime from, LocalDateTime to, Integer branchId) {
        List<DaySummaryDTO> daily = getDailySummary(from, to, branchId);
        return daily.stream()
                .max((a, b) -> a.getGrossIncome().compareTo(b.getGrossIncome()))
                .orElse(null);
    }
 
    // ── 3. Día de la semana más activo ────────────────────────────────────────
    // Agrupa por DOW (0=domingo..6=sábado) y calcula ingresos totales
    // acumulados en todos los días de ese tipo dentro del período.
    // Ejemplo: si el período es junio, suma todos los sábados de junio.
 
    @Transactional(readOnly = true)
    public List<DayOfWeekSummaryDTO> getSalesByDayOfWeek(LocalDateTime from, LocalDateTime to,
                                                          Integer branchId) {
        LocalDateTime f = safeFrom(from);
        LocalDateTime t = safeTo(to);
 
        List<Object[]> raw = branchId == null
                ? reportRepository.findSalesByDayOfWeekRaw(f, t)
                : reportRepository.findSalesByDayOfWeekByBranchRaw(f, t, branchId);
 
        return raw.stream()
                .map(row -> {
                    int    dow        = ((Number) row[0]).intValue();
                    long   totalSales = ((Number) row[1]).longValue();
                    BigDecimal income = new BigDecimal(row[2].toString());
                    return DayOfWeekSummaryDTO.builder()
                            .dayOfWeek(dow)
                            .dayName(DAY_NAMES.getOrDefault(dow, "Día " + dow))
                            .totalSales(totalSales)
                            .grossIncome(income)
                            .build();
                })
                .toList();
    }
 
    // ── 4. Proveedor con más ventas ───────────────────────────────────────────
 
    @Transactional(readOnly = true)
    public List<ProviderSalesDTO> getSalesByProvider(LocalDateTime from, LocalDateTime to,
                                                      Integer branchId) {
        LocalDateTime f = safeFrom(from);
        LocalDateTime t = safeTo(to);
        return branchId == null
                ? reportRepository.findSalesByProvider(f, t)
                : reportRepository.findSalesByProviderAndBranch(f, t, branchId);
    }
 
    // ── 5. Balance neto: ingresos vs gastos ───────────────────────────────────
    // Calcula: utilidadNeta = grossIncome - totalExpenses
    //          margenBruto  = estimatedProfit / grossIncome * 100
    // Incluye desglose de gastos por tipo para el dashboard.
 
    @Transactional(readOnly = true)
    public NetBalanceDTO getNetBalance(LocalDateTime from, LocalDateTime to, Integer branchId) {
        LocalDateTime f = safeFrom(from);
        LocalDateTime t = safeTo(to);
 
        BigDecimal grossIncome = branchId == null
                ? reportRepository.sumGrossIncome(f, t)
                : reportRepository.sumGrossIncomeByBranch(f, t, branchId);
 
        BigDecimal estimatedProfit = branchId == null
                ? reportRepository.calculateEstimatedProfit(f, t)
                : reportRepository.calculateEstimatedProfitByBranch(f, t, branchId);
 
        BigDecimal totalExpenses = branchId == null
                ? reportRepository.sumTotalExpenses(f, t)
                : reportRepository.sumTotalExpensesByBranch(f, t, branchId);
 
        BigDecimal netProfit = grossIncome.subtract(totalExpenses);
 
        // Margen bruto: evitar división por cero
        BigDecimal grossMarginPct = BigDecimal.ZERO;
        if (grossIncome.compareTo(BigDecimal.ZERO) > 0) {
            grossMarginPct = estimatedProfit
                    .divide(grossIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
 
        // Desglose de gastos por tipo
        List<Object[]> expensesRaw = branchId == null
                ? reportRepository.findExpensesByTypeRaw(f, t)
                : reportRepository.findExpensesByTypeByBranchRaw(f, t, branchId);
 
        List<ExpenseByTypeDTO> expensesByType = expensesRaw.stream()
                .map(row -> ExpenseByTypeDTO.builder()
                        .type(String.valueOf(row[0]))
                        .total(new BigDecimal(row[1].toString()))
                        .build())
                .toList();
 
        return NetBalanceDTO.builder()
                .grossIncome(grossIncome)
                .estimatedProfit(estimatedProfit)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .grossMarginPct(grossMarginPct)
                .expensesByType(expensesByType)
                .build();
    }
 
    // =========================================================================
    // HELPERS
    // =========================================================================
 
    private LocalDateTime[] resolvePeriod(ReportPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case DAILY   -> new LocalDateTime[]{ now.toLocalDate().atStartOfDay(), now };
            case WEEKLY  -> new LocalDateTime[]{ now.minusWeeks(1),  now };
            case MONTHLY -> new LocalDateTime[]{ now.minusMonths(1), now };
            case YEARLY  -> new LocalDateTime[]{ now.minusYears(1),  now };
        };
    }
 
    private LocalDate[] resolvePeriodAsDate(ReportPeriod period) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case DAILY   -> new LocalDate[]{ today,                today };
            case WEEKLY  -> new LocalDate[]{ today.minusWeeks(1),  today };
            case MONTHLY -> new LocalDate[]{ today.minusMonths(1), today };
            case YEARLY  -> new LocalDate[]{ today.minusYears(1),  today };
        };
    }
}