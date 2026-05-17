package com.cocobongo.cerveceria.reports.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cocobongo.cerveceria.common.dto.ApiResponse;
import com.cocobongo.cerveceria.reports.dto.BranchSalesReportDTO;
import com.cocobongo.cerveceria.reports.dto.ReportPeriod;
import com.cocobongo.cerveceria.reports.dto.SaleReportDTO;
import com.cocobongo.cerveceria.reports.dto.SalesSummaryDTO;
import com.cocobongo.cerveceria.reports.services.ReportsService;

import lombok.Data;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/reports")
@Data
public class ReportsController {

    private final ReportsService reportsService;

    // REPORTS — GET /api/v1/reports/sales?from=&to=&branchId=&page=&size=
    // RF-REP-01
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<Page<SaleReportDTO>>> getSalesByPeriod(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<SaleReportDTO> result = reportsService.getSalesByPeriod(from, to, branchId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // REPORTS — GET /api/v1/reports/summary?from=&to=&branchId=
    // RF-REP-02, RF-REP-03
   @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SalesSummaryDTO>> getSummary(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer branchId) {

        return ResponseEntity.ok(
                ApiResponse.ok(reportsService.getSummary(from, to, branchId)));
    }

    // REPORTS — GET /api/v1/reports/sales/by-branch?period=
    // RF-REP-01, RF-REP-02
    @GetMapping("/sales/by-branch")
    public ResponseEntity<ApiResponse<List<BranchSalesReportDTO>>> getSalesByBranch(
            @RequestParam(defaultValue = "MONTH") ReportPeriod period) {

        return ResponseEntity.ok(
                ApiResponse.ok(reportsService.getSalesByBranch(period)));
    }
}
