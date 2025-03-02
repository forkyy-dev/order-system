package com.ordersystem.order.ui.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateOrderStockRequest {
    private final Long stockId;
    private final int quantity;
    private final double price;
}
