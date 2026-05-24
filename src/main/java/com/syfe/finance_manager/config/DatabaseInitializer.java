package com.syfe.finance_manager.config;

import com.syfe.finance_manager.entity.Category;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DatabaseInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        // Seed default categories if they do not already exist in the system
        List<Category> defaults = categoryRepository.findByIsDefaultTrue();
        if (defaults.isEmpty()) {
            List<Category> defaultCategories = Arrays.asList(
                    // INCOME Categories
                    Category.builder().name("Salary").type(CategoryType.INCOME).isDefault(true).user(null).build(),
                    Category.builder().name("Freelance").type(CategoryType.INCOME).isDefault(true).user(null).build(),
                    Category.builder().name("Investments").type(CategoryType.INCOME).isDefault(true).user(null).build(),
                    Category.builder().name("Other Income").type(CategoryType.INCOME).isDefault(true).user(null).build(),

                    // EXPENSE Categories
                    Category.builder().name("Groceries").type(CategoryType.EXPENSE).isDefault(true).user(null).build(),
                    Category.builder().name("Rent").type(CategoryType.EXPENSE).isDefault(true).user(null).build(),
                    Category.builder().name("Utilities").type(CategoryType.EXPENSE).isDefault(true).user(null).build(),
                    Category.builder().name("Dining Out").type(CategoryType.EXPENSE).isDefault(true).user(null).build(),
                    Category.builder().name("Entertainment").type(CategoryType.EXPENSE).isDefault(true).user(null).build(),
                    Category.builder().name("Shopping").type(CategoryType.EXPENSE).isDefault(true).user(null).build(),
                    Category.builder().name("Travel").type(CategoryType.EXPENSE).isDefault(true).user(null).build(),
                    Category.builder().name("Other Expense").type(CategoryType.EXPENSE).isDefault(true).user(null).build()
            );

            categoryRepository.saveAll(defaultCategories);
        }
    }
}
