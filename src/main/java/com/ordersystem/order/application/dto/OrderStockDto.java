package com.ordersystem.order.application.dto;

import com.ordersystem.order.domain.OrderStock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStockDto {
    private final Long orderStockId;
    private final Integer quantity;
    private final Double originalPrice;
    private final Double totalPrice;
    private final Long stockId;
    private final Long orderId;

    public static OrderStockDto from(OrderStock orderStock) {
        return new OrderStockDto(
                orderStock.getStockId(),
                orderStock.getQuantity(),
                orderStock.getOriginalPrice(),
                orderStock.getTotalPrice(),
                orderStock.getStockId(),
                orderStock.getOrderId()
        );
    }
}
