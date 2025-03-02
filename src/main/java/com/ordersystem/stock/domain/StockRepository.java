package com.ordersystem.stock.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Boolean existsByNameAndCategoryId(String name, Long categoryId);

    Slice<Stock> findByCategoryIdAndNameContains(Long categoryId, String name, Pageable pageable);

    List<Stock> findAllByIdIsIn(List<Long> list);
}
