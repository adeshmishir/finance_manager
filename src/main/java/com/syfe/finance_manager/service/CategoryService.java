package com.syfe.finance_manager.service;

import com.syfe.finance_manager.dto.CategoryRequest;
import com.syfe.finance_manager.dto.CategoryResponse;
import com.syfe.finance_manager.entity.Category;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.BusinessValidationException;
import com.syfe.finance_manager.exception.ResourceNotFoundException;
import com.syfe.finance_manager.exception.UnauthorizedAccessException;
import com.syfe.finance_manager.repository.CategoryRepository;
import com.syfe.finance_manager.repository.TransactionRepository;
import com.syfe.finance_manager.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    // Get all categories accessible to the current user (defaults + their custom ones)
    public List<CategoryResponse> getCategories() {
        User currentUser = getCurrentUser();
        return categoryRepository.findByIsDefaultTrueOrUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        User currentUser = getCurrentUser();

        // Prevent creating a category with same name as an existing default
        if (categoryRepository.existsByNameIgnoreCaseAndIsDefaultTrue(request.getName())) {
            throw new BusinessValidationException(
                    "A default category named '" + request.getName() + "' already exists");
        }

        // Prevent duplicate custom category name for this user
        if (categoryRepository.existsByNameIgnoreCaseAndUserId(request.getName(), currentUser.getId())) {
            throw new BusinessValidationException(
                    "You already have a custom category named '" + request.getName() + "'");
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .isDefault(false)
                .user(currentUser)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long categoryId) {
        User currentUser = getCurrentUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Only the owning user can delete their custom categories
        if (category.isDefault()) {
            throw new UnauthorizedAccessException("Default categories cannot be deleted");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to delete this category");
        }

        // Prevent deletion if category is linked to existing transactions
        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new BusinessValidationException(
                    "Cannot delete category because it is linked to existing transactions");
        }

        categoryRepository.delete(category);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .isDefault(category.isDefault())
                .build();
    }
}
