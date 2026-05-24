package com.syfe.finance_manager.service;

import com.syfe.finance_manager.dto.GoalProgressResponse;
import com.syfe.finance_manager.dto.SavingsGoalRequest;
import com.syfe.finance_manager.dto.SavingsGoalResponse;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.SavingsGoal;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.ResourceNotFoundException;
import com.syfe.finance_manager.repository.SavingsGoalRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock private SavingsGoalRepository savingsGoalRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private User testUser;
    private SavingsGoal testGoal;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("john").fullName("John Doe").password("pass").build();
        testGoal = SavingsGoal.builder()
                .id(1L).goalName("New Laptop").targetAmount(new BigDecimal("50000"))
                .targetDate(LocalDate.now().plusMonths(6)).startDate(LocalDate.now().minusMonths(1))
                .user(testUser).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(testUser));
    }

    @Test
    void getAllGoals_returnsUserGoals() {
        when(savingsGoalRepository.findByUserId(1L)).thenReturn(List.of(testGoal));

        List<SavingsGoalResponse> result = savingsGoalService.getAllGoals();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGoalName()).isEqualTo("New Laptop");
    }

    @Test
    void createGoal_success() {
        SavingsGoalRequest request = new SavingsGoalRequest(
                "Emergency Fund", new BigDecimal("100000"),
                LocalDate.now().plusYears(1), LocalDate.now());

        when(savingsGoalRepository.save(any())).thenReturn(
                SavingsGoal.builder().id(2L).goalName("Emergency Fund")
                        .targetAmount(request.getTargetAmount())
                        .targetDate(request.getTargetDate()).startDate(request.getStartDate())
                        .user(testUser).build());

        SavingsGoalResponse result = savingsGoalService.createGoal(request);

        assertThat(result.getGoalName()).isEqualTo("Emergency Fund");
        assertThat(result.getTargetAmount()).isEqualByComparingTo("100000");
    }

    @Test
    void deleteGoal_success() {
        when(savingsGoalRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testGoal));

        savingsGoalService.deleteGoal(1L);

        verify(savingsGoalRepository).delete(testGoal);
    }

    @Test
    void getGoal_throwsWhenNotFound() {
        when(savingsGoalRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savingsGoalService.getGoal(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProgress_calculatesCorrectly() {
        when(savingsGoalRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testGoal));
        when(transactionRepository.sumByUserAndTypeAfterDate(1L, CategoryType.INCOME, testGoal.getStartDate()))
                .thenReturn(new BigDecimal("30000"));
        when(transactionRepository.sumByUserAndTypeAfterDate(1L, CategoryType.EXPENSE, testGoal.getStartDate()))
                .thenReturn(new BigDecimal("5000"));

        GoalProgressResponse result = savingsGoalService.getProgress(1L);

        // currentProgress = 30000 - 5000 = 25000
        assertThat(result.getCurrentProgress()).isEqualByComparingTo("25000");
        // percentage = (25000 / 50000) * 100 = 50.0
        assertThat(result.getProgressPercentage()).isEqualTo(50.0);
    }

    @Test
    void getProgress_canExceed100Percent() {
        when(savingsGoalRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testGoal));
        when(transactionRepository.sumByUserAndTypeAfterDate(1L, CategoryType.INCOME, testGoal.getStartDate()))
                .thenReturn(new BigDecimal("70000"));
        when(transactionRepository.sumByUserAndTypeAfterDate(1L, CategoryType.EXPENSE, testGoal.getStartDate()))
                .thenReturn(new BigDecimal("5000"));

        GoalProgressResponse result = savingsGoalService.getProgress(1L);

        // currentProgress = 65000, target = 50000 → 130%
        assertThat(result.getProgressPercentage()).isGreaterThan(100.0);
    }
}
