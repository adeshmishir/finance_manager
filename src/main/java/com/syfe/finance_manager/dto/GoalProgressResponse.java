package com.syfe.finance_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgressResponse {

    private Long goalId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentProgress;   // totalIncome - totalExpense since startDate
    private double progressPercentage;    // can exceed 100 if goal is surpassed
}
