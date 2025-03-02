package com.ordersystem.order.application.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateOrderStockDto {
    private final Long stockId;
    private final int quantity;

    public static CreateOrderStockDto from(Long stockId, int quantity) {
        return new CreateOrderStockDto(stockId, quantity);
    }
}
