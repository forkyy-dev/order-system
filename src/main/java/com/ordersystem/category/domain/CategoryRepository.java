package com.ordersystem.category.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long>{
    Boolean existsByName(String categoryName);
}
