package com.syfe.finance_manager.controller;

import com.syfe.finance_manager.dto.ApiResponse;
import com.syfe.finance_manager.dto.MonthlyReportResponse;
import com.syfe.finance_manager.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyReportResponse>> getMonthlySummary(
            @RequestParam int month,
            @RequestParam int year) {
        MonthlyReportResponse report = reportService.getMonthlySummary(month, year);
        return ResponseEntity.ok(ApiResponse.success("Monthly report generated successfully", report));
    }
}
