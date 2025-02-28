package com.ordersystem.stock.application.dto;

import com.ordersystem.stock.domain.Stock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StockCreateDto {

    private final String name;
    private final double price;
    private final int quantity;
    private final long categoryId;

    public Stock toEntity() {
        return new Stock(name, price, quantity, categoryId);
    }
}
