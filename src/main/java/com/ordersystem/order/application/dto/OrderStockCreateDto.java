package com.ordersystem.order.application.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStockCreateDto {
    private final Long stockId;
    private final int quantity;

    public static OrderStockCreateDto from(Long stockId, int quantity) {
        return new OrderStockCreateDto(stockId, quantity);
    }
}
