package com.cocobongo.cerveceria.reports.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cocobongo.cerveceria.reports.dto.BranchSalesReportDTO;
import com.cocobongo.cerveceria.reports.dto.PeriodSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.ReportPeriod;
import com.cocobongo.cerveceria.reports.dto.SaleReportDTO;
import com.cocobongo.cerveceria.reports.dto.SalesSummaryDTO;
import com.cocobongo.cerveceria.reports.dto.TopProductDTO;
import com.cocobongo.cerveceria.reports.repositories.ReportRepository;
import com.cocobongo.cerveceria.sales.entities.SaleEntity;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de negocio para generar reportes de ventas y sucursales.
 * Centraliza la lógica de consulta y transformación de datos para la capa web.
 */
@RequiredArgsConstructor
@Service
public class ReportsService {

    private final ReportRepository reportRepository;

    // Rango seguro cuando no se especifica fecha
    private static final LocalDateTime MIN_DATE = LocalDateTime.of(2000,  1,  1,  0,  0,  0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(2100, 12, 31, 23, 59, 59);

    private LocalDateTime safeFrom(LocalDateTime from) { return from != null ? from : MIN_DATE; }
    private LocalDateTime safeTo  (LocalDateTime to)   { return to   != null ? to   : MAX_DATE; }

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
                .branchName(sale.getBranch().getName())
                .clientName(sale.getClient()   != null ? sale.getClient().getName() : null)
                .registeredBy(sale.getUser()   != null ? sale.getUser().getName()   : null)
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
 
    // ── Helper: convierte ReportPeriod a rango de fechas ─────────────────────

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