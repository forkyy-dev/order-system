package com.ordersystem.order.domain;

import com.ordersystem.common.domain.BaseTimeEntity;
import com.ordersystem.common.utils.StatusChangeReasonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long orderId;
    private OrderStatus status;
    @Convert(converter = StatusChangeReasonConverter.class)
    private StatusChangeReason reason;

    public OrderStatusHistory(Long orderId, OrderStatus status, StatusChangeReason reason) {
        this.orderId = orderId;
        this.status = status;
        this.reason = reason;
    }
}
