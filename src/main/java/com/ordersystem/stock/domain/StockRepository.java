package com.ordersystem.stock.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Boolean existsByNameAndCategoryId(String name, Long categoryId);
}
