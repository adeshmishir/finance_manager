package com.syfe.finance_manager.dto;

import com.syfe.finance_manager.entity.CategoryType;
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
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String description;

    // Flattened category details to keep the response clean
    private Long categoryId;
    private String categoryName;
    private CategoryType categoryType;
}
