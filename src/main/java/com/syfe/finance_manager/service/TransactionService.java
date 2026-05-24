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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public TransactionResponse createTransaction(TransactionRequest request) {
        User currentUser = getCurrentUser();
        Category category = resolveCategory(request.getCategoryId(), currentUser);

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .category(category)
                .user(currentUser)
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    public TransactionResponse updateTransaction(Long transactionId, TransactionRequest request) {
        User currentUser = getCurrentUser();
        Transaction transaction = getOwnedTransaction(transactionId, currentUser);

        Category category = resolveCategory(request.getCategoryId(), currentUser);

        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(category);

        return mapToResponse(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long transactionId) {
        User currentUser = getCurrentUser();
        Transaction transaction = getOwnedTransaction(transactionId, currentUser);
        transactionRepository.delete(transaction);
    }

    // Returns filtered transactions for the current user. All params are optional.
    public List<TransactionResponse> getTransactions(Long categoryId, LocalDate startDate,
                                                     LocalDate endDate, CategoryType type) {
        User currentUser = getCurrentUser();

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessValidationException("Start date cannot be after end date");
        }

        return transactionRepository
                .filterTransactions(currentUser.getId(), categoryId, startDate, endDate, type)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Resolves and validates that the category is accessible to this user
    private Category resolveCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        boolean isDefault = category.isDefault();
        boolean isOwnedByUser = category.getUser() != null
                && category.getUser().getId().equals(user.getId());

        if (!isDefault && !isOwnedByUser) {
            throw new UnauthorizedAccessException("You do not have access to this category");
        }

        return category;
    }

    // Fetches a transaction and verifies the current user owns it
    private Transaction getOwnedTransaction(Long transactionId, User user) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access this transaction");
        }

        return transaction;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .description(transaction.getDescription())
                .categoryId(transaction.getCategory().getId())
                .categoryName(transaction.getCategory().getName())
                .categoryType(transaction.getCategory().getType())
                .build();
    }
}
