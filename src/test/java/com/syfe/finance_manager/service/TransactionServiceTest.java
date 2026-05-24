package com.syfe.finance_manager.service;

import com.syfe.finance_manager.dto.TransactionRequest;
import com.syfe.finance_manager.dto.TransactionResponse;
import com.syfe.finance_manager.entity.Category;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.Transaction;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Category incomeCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("john").fullName("John Doe").password("pass").build();
        incomeCategory = Category.builder().id(1L).name("Salary").type(CategoryType.INCOME).isDefault(true).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(testUser));
    }

    @Test
    void createTransaction_success() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("5000"), LocalDate.now().minusDays(1), "Monthly salary", 1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(incomeCategory));

        Transaction saved = Transaction.builder()
                .id(10L).amount(request.getAmount()).date(request.getDate())
                .description(request.getDescription()).category(incomeCategory).user(testUser).build();
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse result = transactionService.createTransaction(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getCategoryName()).isEqualTo("Salary");
        assertThat(result.getAmount()).isEqualByComparingTo("5000");
    }

    @Test
    void createTransaction_throwsWhenCategoryNotFound() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("500"), LocalDate.now(), "desc", 99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void createTransaction_throwsWhenCategoryBelongsToAnotherUser() {
        User otherUser = User.builder().id(99L).username("other").build();
        Category otherCategory = Category.builder().id(5L).name("Side hustle")
                .isDefault(false).user(otherUser).build();

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100"), LocalDate.now(), "desc", 5L);

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(otherCategory));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void deleteTransaction_success() {
        Transaction transaction = Transaction.builder()
                .id(1L).user(testUser).amount(BigDecimal.TEN).date(LocalDate.now())
                .category(incomeCategory).build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(1L);

        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_throwsWhenNotOwned() {
        User otherUser = User.builder().id(99L).username("other").build();
        Transaction transaction = Transaction.builder()
                .id(1L).user(otherUser).amount(BigDecimal.TEN).date(LocalDate.now())
                .category(incomeCategory).build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.deleteTransaction(1L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void getTransactions_throwsWhenStartDateAfterEndDate() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().minusDays(5);

        assertThatThrownBy(() -> transactionService.getTransactions(null, start, end, null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Start date cannot be after end date");
    }

    @Test
    void getTransactions_returnsFilteredResults() {
        Transaction t = Transaction.builder().id(1L).amount(BigDecimal.TEN)
                .date(LocalDate.now()).category(incomeCategory).user(testUser).build();

        when(transactionRepository.filterTransactions(1L, null, null, null, null))
                .thenReturn(List.of(t));

        List<TransactionResponse> result = transactionService.getTransactions(null, null, null, null);

        assertThat(result).hasSize(1);
    }
}
