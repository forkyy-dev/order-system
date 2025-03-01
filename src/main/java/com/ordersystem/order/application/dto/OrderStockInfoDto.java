package com.ordersystem.order.application.dto;

import com.ordersystem.order.domain.OrderStock;
import com.ordersystem.stock.domain.Stock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStockInfoDto {

    private final Long stockId;
    private final String stockName;
    private final double originalPrice;
    private final double totalPrice;
    private final int quantity;

    public static OrderStockInfoDto from(OrderStock orderStock, Stock stock) {
        return new OrderStockInfoDto(
                orderStock.getStockId(),
                stock.getName(),
                orderStock.getOriginalPrice(),
                orderStock.getTotalPrice(),
                orderStock.getQuantity()
        );
    }
}
