package com.syfe.finance_manager.repository;

import com.syfe.finance_manager.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserId(Long userId);

    Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);
}
