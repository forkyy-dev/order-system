package com.ordersystem.order.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Slice<Order> findAllByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
}
