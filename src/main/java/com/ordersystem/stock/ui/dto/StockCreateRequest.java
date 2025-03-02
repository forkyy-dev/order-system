package com.ordersystem.stock.ui.dto;

import com.ordersystem.stock.application.dto.CreateStockDto;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class StockCreateRequest {
    private final String stockName;
    private final Double price;
    private final Integer quantity;
    private final Long categoryId;

    public CreateStockDto toStockCreateDto() {
        return new CreateStockDto(stockName, price, quantity, categoryId);
    }
}
