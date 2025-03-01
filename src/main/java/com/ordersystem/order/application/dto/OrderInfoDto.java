package com.ordersystem.order.application.dto;

import com.ordersystem.order.domain.Order;
import com.ordersystem.order.domain.OrderStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderInfoDto {

    private final Long orderId;
    private final String orderNo;
    private final Double totalPrice;
    private final LocalDateTime orderDate;
    private final OrderStatus orderStatus;
    private final List<OrderStockInfoDto> stocks;

    public static OrderInfoDto from(Order order, List<OrderStockInfoDto> orderStockInfos) {
        return new OrderInfoDto(order.getId(), order.getOrderNo(), order.getTotalPrice(), order.getOrderDate(), order.getStatus(), orderStockInfos);
    }
}
