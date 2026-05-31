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

import lombok.RequiredArgsConstructor;

/**
 * Servicio de negocio para generar reportes de ventas y sucursales.
 * Centraliza la lógica de consulta y transformación de datos para la capa web.
 */
@RequiredArgsConstructor
@Service
public class ReportsService {

    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public Page<SaleReportDTO> getSalesByPeriod(LocalDateTime from, LocalDateTime to, Integer branchId, Pageable pageable) {
        return reportRepository.findByPeriodAndBranch(from, to, branchId, pageable)
            .map(sale -> SaleReportDTO.builder()
                    .idSale(sale.getIdSale())
                    .saleDate(sale.getSaleDate())
                    .total(sale.getTotal())
                    .branchName(sale.getBranch().getName())
                    .clientName(sale.getClient() != null ? sale.getClient().getName() : null)
                    .registeredBy(sale.getUser()   != null ? sale.getUser().getName() : null)
                    .paymentType(sale.getPaymentType())
                    .status(sale.getStatus())
                    .build());
    }

    @Transactional(readOnly = true)
    public SalesSummaryDTO getSummary(LocalDateTime from, LocalDateTime to, Integer branchId) {

        Long       totalSales      = reportRepository.countSales(from, to, branchId);
        BigDecimal grossIncome     = reportRepository.sumGrossIncome(from, to, branchId);
        BigDecimal estimatedProfit = reportRepository.calculateEstimatedProfit(from, to, branchId);
        BigDecimal estimatedCost   = grossIncome.subtract(estimatedProfit);

        List<TopProductDTO> topProducts = reportRepository.findTopProducts(
                from, to, branchId, PageRequest.of(0, 5));

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