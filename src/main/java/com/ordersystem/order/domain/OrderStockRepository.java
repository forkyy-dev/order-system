package com.ordersystem.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStockRepository extends JpaRepository<OrderStock, Long> {
    List<OrderStock> findAllByOrderId(Long orderId);
}
