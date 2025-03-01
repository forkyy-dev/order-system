package com.ordersystem.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStockRepository extends JpaRepository<OrderStock, Long> {
}
