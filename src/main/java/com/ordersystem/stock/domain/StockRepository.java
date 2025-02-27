package com.ordersystem.stock.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByName(String name);

    Boolean existsByName(String name);
}
