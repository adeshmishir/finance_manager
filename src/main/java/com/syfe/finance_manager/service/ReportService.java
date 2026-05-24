package com.syfe.finance_manager.service;

import com.syfe.finance_manager.dto.MonthlyReportResponse;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.ResourceNotFoundException;
import com.syfe.finance_manager.repository.TransactionRepository;
import com.syfe.finance_manager.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public ReportService(TransactionRepository transactionRepository,
                         UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public MonthlyReportResponse getMonthlySummary(int month, int year) {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        Map<String, BigDecimal> incomeByCategory =
                buildCategoryMap(transactionRepository.sumByCategoryForMonth(userId, CategoryType.INCOME, month, year));

        Map<String, BigDecimal> expenseByCategory =
                buildCategoryMap(transactionRepository.sumByCategoryForMonth(userId, CategoryType.EXPENSE, month, year));

        BigDecimal totalIncome =
                transactionRepository.sumTotalForMonth(userId, CategoryType.INCOME, month, year);

        BigDecimal totalExpense =
                transactionRepository.sumTotalForMonth(userId, CategoryType.EXPENSE, month, year);

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncomeByCategory(incomeByCategory)
                .totalExpenseByCategory(expenseByCategory)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .build();
    }

    // Converts raw Object[] rows (categoryName, sum) into a readable map
    private Map<String, BigDecimal> buildCategoryMap(List<Object[]> rows) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String categoryName = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            result.put(categoryName, total);
        }
        return result;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
