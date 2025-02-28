package com.ordersystem.stock.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StockModifyDto {
    private final long id;
    private final String name;
    private final double price;
    private final int currentQuantity;
    private final int maxQuantity;
    private final long categoryId;
}
