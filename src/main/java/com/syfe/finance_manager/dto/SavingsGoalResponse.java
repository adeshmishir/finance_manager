package com.syfe.finance_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalResponse {

    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate startDate;
}
