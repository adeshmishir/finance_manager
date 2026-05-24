package com.syfe.finance_manager.service;

import com.syfe.finance_manager.dto.CategoryRequest;
import com.syfe.finance_manager.dto.CategoryResponse;
import com.syfe.finance_manager.entity.Category;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.BusinessValidationException;
import com.syfe.finance_manager.exception.ResourceNotFoundException;
import com.syfe.finance_manager.exception.UnauthorizedAccessException;
import com.syfe.finance_manager.repository.CategoryRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private CategoryService categoryService;

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
    void getCategories_returnsDefaultAndUserCategories() {
        Category defaultCat = Category.builder().id(1L).name("Salary").type(CategoryType.INCOME).isDefault(true).build();
        Category userCat = Category.builder().id(2L).name("Freelance").type(CategoryType.INCOME).isDefault(false).user(testUser).build();

        when(categoryRepository.findByIsDefaultTrueOrUserId(1L)).thenReturn(List.of(defaultCat, userCat));

        List<CategoryResponse> result = categoryService.getCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Salary");
        assertThat(result.get(1).getName()).isEqualTo("Freelance");
    }

    @Test
    void createCategory_success() {
        CategoryRequest request = new CategoryRequest("Freelance", CategoryType.INCOME);

        when(categoryRepository.existsByNameIgnoreCaseAndIsDefaultTrue("Freelance")).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserId("Freelance", 1L)).thenReturn(false);

        Category saved = Category.builder().id(3L).name("Freelance").type(CategoryType.INCOME).isDefault(false).user(testUser).build();
        when(categoryRepository.save(any())).thenReturn(saved);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result.getName()).isEqualTo("Freelance");
        assertThat(result.isDefault()).isFalse();
    }

    @Test
    void createCategory_throwsWhenDuplicatesDefaultCategory() {
        CategoryRequest request = new CategoryRequest("Salary", CategoryType.INCOME);
        when(categoryRepository.existsByNameIgnoreCaseAndIsDefaultTrue("Salary")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("default category");
    }

    @Test
    void createCategory_throwsWhenDuplicateUserCategory() {
        CategoryRequest request = new CategoryRequest("Freelance", CategoryType.INCOME);
        when(categoryRepository.existsByNameIgnoreCaseAndIsDefaultTrue("Freelance")).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserId("Freelance", 1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already have");
    }

    @Test
    void deleteCategory_throwsWhenCategoryIsDefault() {
        Category defaultCat = Category.builder().id(1L).name("Salary").isDefault(true).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(defaultCat));

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Default categories cannot be deleted");
    }

    @Test
    void deleteCategory_throwsWhenLinkedToTransactions() {
        Category userCat = Category.builder().id(2L).name("Freelance").isDefault(false).user(testUser).build();
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(userCat));
        when(transactionRepository.existsByCategoryId(2L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(2L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("linked to existing transactions");
    }

    @Test
    void deleteCategory_success() {
        Category userCat = Category.builder().id(2L).name("Freelance").isDefault(false).user(testUser).build();
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(userCat));
        when(transactionRepository.existsByCategoryId(2L)).thenReturn(false);

        categoryService.deleteCategory(2L);

        verify(categoryRepository).delete(userCat);
    }

    @Test
    void deleteCategory_throwsWhenNotOwned() {
        User otherUser = User.builder().id(99L).username("other").build();
        Category userCat = Category.builder().id(2L).name("Freelance").isDefault(false).user(otherUser).build();
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(userCat));

        assertThatThrownBy(() -> categoryService.deleteCategory(2L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void deleteCategory_throwsWhenCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
