package com.syfe.finance_manager.repository;

import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByCategoryId(Long categoryId);

    List<Transaction> findByUserIdOrderByDateDesc(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:type IS NULL OR t.category.type = :type) " +
           "ORDER BY t.date DESC")
    List<Transaction> filterTransactions(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") CategoryType type
    );
}
