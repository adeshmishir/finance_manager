package com.syfe.finance_manager.repository;

import com.syfe.finance_manager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsDefaultTrue();

    List<Category> findByIsDefaultTrueOrUserId(Long userId);

    boolean existsByNameIgnoreCaseAndIsDefaultTrue(String name);

    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
}
