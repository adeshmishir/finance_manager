package com.syfe.finance_manager.controller;

import com.syfe.finance_manager.dto.ApiResponse;
import com.syfe.finance_manager.dto.GoalProgressResponse;
import com.syfe.finance_manager.dto.SavingsGoalRequest;
import com.syfe.finance_manager.dto.SavingsGoalResponse;
import com.syfe.finance_manager.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavingsGoalResponse>>> getAllGoals() {
        List<SavingsGoalResponse> goals = savingsGoalService.getAllGoals();
        return ResponseEntity.ok(ApiResponse.success("Goals retrieved successfully", goals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> getGoal(@PathVariable Long id) {
        SavingsGoalResponse goal = savingsGoalService.getGoal(id);
        return ResponseEntity.ok(ApiResponse.success("Goal retrieved successfully", goal));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> createGoal(
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse goal = savingsGoalService.createGoal(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created successfully", goal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse goal = savingsGoalService.updateGoal(id, request);
        return ResponseEntity.ok(ApiResponse.success("Goal updated successfully", goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long id) {
        savingsGoalService.deleteGoal(id);
        return ResponseEntity.ok(ApiResponse.success("Goal deleted successfully"));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<GoalProgressResponse>> getProgress(@PathVariable Long id) {
        GoalProgressResponse progress = savingsGoalService.getProgress(id);
        return ResponseEntity.ok(ApiResponse.success("Goal progress retrieved successfully", progress));
    }
}
