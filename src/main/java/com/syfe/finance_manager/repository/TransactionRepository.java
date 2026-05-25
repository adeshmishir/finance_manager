package com.syfe.finance_manager.repository;

import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    // Sum of all transactions of a given type for a user on/after a start date (for goal progress)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.category.type = :type " +
           "AND t.date >= :startDate")
    BigDecimal sumByUserAndTypeAfterDate(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("startDate") LocalDate startDate
    );

    // Sum grouped by category for a specific month/year and type (for reports)
    @Query("SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.category.type = :type " +
           "AND MONTH(t.date) = :month " +
           "AND YEAR(t.date) = :year " +
           "GROUP BY t.category.name")
    List<Object[]> sumByCategoryForMonth(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("month") int month,
            @Param("year") int year
    );

    // Total sum for a user for a given month/year and type (for net savings)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.category.type = :type " +
           "AND MONTH(t.date) = :month " +
           "AND YEAR(t.date) = :year")
    BigDecimal sumTotalForMonth(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("month") int month,
            @Param("year") int year
    );
}
