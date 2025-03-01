package com.ordersystem.order.domain;

import com.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Embedded
    private OrderNumber orderNumber;
    private Double totalPrice;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private Long userId;

    public Order(Double totalPrice, Long userId) {
        this.id = null;
        this.orderNumber = new OrderNumber();
        this.totalPrice = totalPrice;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PRE;
        this.userId = userId;
    }

    public String getOrderNumber() {
        return this.orderNumber.getOrderNumber();
    }

    public void success() {
        this.status = OrderStatus.SUCCESS;
    }
}
