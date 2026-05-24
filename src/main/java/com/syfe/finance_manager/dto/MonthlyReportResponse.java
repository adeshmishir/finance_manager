package com.syfe.finance_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponse {

    private int month;
    private int year;
    private Map<String, BigDecimal> totalIncomeByCategory;
    private Map<String, BigDecimal> totalExpenseByCategory;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
}
