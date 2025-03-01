package com.ordersystem.order.application.dto;

import com.ordersystem.order.domain.Order;
import com.ordersystem.order.domain.OrderStatus;
import com.ordersystem.order.domain.OrderStock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDto {
    private final Long orderId;
    private final String orderNo;
    private final List<OrderStockDto> orderStocks;
    private final double totalPrice;
    private final OrderStatus orderStatus;
    private final LocalDateTime orderDate;

    public static OrderDto from(Order order, List<OrderStock> orderStocks) {
        List<OrderStockDto> orderStockDtos =orderStocks.stream()
                .map(OrderStockDto::from)
                .toList();

        return new OrderDto(
                order.getId(),
                order.getOrderNo(),
                orderStockDtos,
                order.getTotalPrice(),
                order.getStatus(),
                order.getOrderDate()
        );
    }
}
