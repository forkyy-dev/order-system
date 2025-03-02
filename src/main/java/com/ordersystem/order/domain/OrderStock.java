package com.ordersystem.order.domain;

import com.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStock extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer quantity;
    private Double originalPrice;
    private Double totalPrice;
    private Long stockId;
    private Long orderId;

    public OrderStock(Integer quantity, Double price, Long stockId, Long orderId) {
        this.quantity = quantity;
        this.originalPrice = price;
        this.totalPrice = price * quantity;
        this.stockId = stockId;
        this.orderId = orderId;
    }
}
