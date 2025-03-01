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

    private Order(Long id, OrderNumber orderNumber, Double totalPrice, LocalDateTime orderDate, OrderStatus status, Long userId) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.userId = userId;
    }

    public Order(Double totalPrice, OrderStatus status, Long userId) {
        this(null, new OrderNumber(), totalPrice, LocalDateTime.now(), status, userId);
    }

    public Order(Double totalPrice, Long userId) {
        this(null, new OrderNumber(), totalPrice, LocalDateTime.now(), OrderStatus.PRE, userId);
    }

    public String getOrderNumber() {
        return this.orderNumber.getOrderNumber();
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRM;
    }

    public void cancel() {
        this.status = OrderStatus.CANCEL;
    }

    public boolean isConfirmed() {
        return OrderStatus.CONFIRM.equals(this.status);
    }

    public boolean isCanceled() {
        return OrderStatus.CANCEL.equals(this.status);
    }
}
