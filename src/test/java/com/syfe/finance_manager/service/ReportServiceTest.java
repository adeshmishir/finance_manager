package com.syfe.finance_manager.service;

import com.syfe.finance_manager.dto.MonthlyReportResponse;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.repository.TransactionRepository;
import com.syfe.finance_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private ReportService reportService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("john").fullName("John Doe").password("pass").build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(testUser));
    }

    @Test
    void getMonthlySummary_returnsCorrectReport() {
        Object[] incomeRow = new Object[]{"Salary", new BigDecimal("50000")};
        Object[] expenseRow = new Object[]{"Rent", new BigDecimal("15000")};

        when(transactionRepository.sumByCategoryForMonth(1L, CategoryType.INCOME, 5, 2025))
                .thenReturn(List.of(new Object[][]{incomeRow}));
        when(transactionRepository.sumByCategoryForMonth(1L, CategoryType.EXPENSE, 5, 2025))
                .thenReturn(List.of(new Object[][]{expenseRow}));
        when(transactionRepository.sumTotalForMonth(1L, CategoryType.INCOME, 5, 2025))
                .thenReturn(new BigDecimal("50000"));
        when(transactionRepository.sumTotalForMonth(1L, CategoryType.EXPENSE, 5, 2025))
                .thenReturn(new BigDecimal("15000"));

        MonthlyReportResponse report = reportService.getMonthlySummary(5, 2025);

        assertThat(report.getMonth()).isEqualTo(5);
        assertThat(report.getYear()).isEqualTo(2025);
        assertThat(report.getTotalIncome()).isEqualByComparingTo("50000");
        assertThat(report.getTotalExpense()).isEqualByComparingTo("15000");
        assertThat(report.getNetSavings()).isEqualByComparingTo("35000");
        assertThat(report.getTotalIncomeByCategory()).containsKey("Salary");
        assertThat(report.getTotalExpenseByCategory()).containsKey("Rent");
    }

    @Test
    void getMonthlySummary_netSavingsIsNegativeWhenExpensesExceedIncome() {
        when(transactionRepository.sumByCategoryForMonth(anyLong(), eq(CategoryType.INCOME), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(transactionRepository.sumByCategoryForMonth(anyLong(), eq(CategoryType.EXPENSE), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(transactionRepository.sumTotalForMonth(1L, CategoryType.INCOME, 6, 2025))
                .thenReturn(new BigDecimal("5000"));
        when(transactionRepository.sumTotalForMonth(1L, CategoryType.EXPENSE, 6, 2025))
                .thenReturn(new BigDecimal("12000"));

        MonthlyReportResponse report = reportService.getMonthlySummary(6, 2025);

        assertThat(report.getNetSavings()).isNegative();
    }
}
