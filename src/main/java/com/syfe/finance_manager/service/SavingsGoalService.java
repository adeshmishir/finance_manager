package com.syfe.finance_manager.service;

import com.syfe.finance_manager.dto.GoalProgressResponse;
import com.syfe.finance_manager.dto.SavingsGoalRequest;
import com.syfe.finance_manager.dto.SavingsGoalResponse;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.SavingsGoal;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.ResourceNotFoundException;
import com.syfe.finance_manager.exception.UnauthorizedAccessException;
import com.syfe.finance_manager.repository.SavingsGoalRepository;
import com.syfe.finance_manager.repository.TransactionRepository;
import com.syfe.finance_manager.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository,
                              TransactionRepository transactionRepository,
                              UserRepository userRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public List<SavingsGoalResponse> getAllGoals() {
        User currentUser = getCurrentUser();
        return savingsGoalRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SavingsGoalResponse getGoal(Long goalId) {
        User currentUser = getCurrentUser();
        SavingsGoal goal = findOwnedGoal(goalId, currentUser.getId());
        return mapToResponse(goal);
    }

    public SavingsGoalResponse createGoal(SavingsGoalRequest request) {
        User currentUser = getCurrentUser();

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(request.getStartDate())
                .user(currentUser)
                .build();

        return mapToResponse(savingsGoalRepository.save(goal));
    }

    public SavingsGoalResponse updateGoal(Long goalId, SavingsGoalRequest request) {
        User currentUser = getCurrentUser();
        SavingsGoal goal = findOwnedGoal(goalId, currentUser.getId());

        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setStartDate(request.getStartDate());

        return mapToResponse(savingsGoalRepository.save(goal));
    }

    public void deleteGoal(Long goalId) {
        User currentUser = getCurrentUser();
        SavingsGoal goal = findOwnedGoal(goalId, currentUser.getId());
        savingsGoalRepository.delete(goal);
    }

    public GoalProgressResponse getProgress(Long goalId) {
        User currentUser = getCurrentUser();
        SavingsGoal goal = findOwnedGoal(goalId, currentUser.getId());

        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAfterDate(
                currentUser.getId(), CategoryType.INCOME, goal.getStartDate());

        BigDecimal totalExpense = transactionRepository.sumByUserAndTypeAfterDate(
                currentUser.getId(), CategoryType.EXPENSE, goal.getStartDate());

        BigDecimal currentProgress = totalIncome.subtract(totalExpense);

        double progressPercentage = currentProgress
                .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        return GoalProgressResponse.builder()
                .goalId(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .currentProgress(currentProgress)
                .progressPercentage(progressPercentage)
                .build();
    }

    private SavingsGoal findOwnedGoal(Long goalId, Long userId) {
        return savingsGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + goalId));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private SavingsGoalResponse mapToResponse(SavingsGoal goal) {
        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .build();
    }
}
