package com.syfe.finance_manager.dto;

import com.syfe.finance_manager.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private CategoryType type;
    private boolean isDefault;
}
