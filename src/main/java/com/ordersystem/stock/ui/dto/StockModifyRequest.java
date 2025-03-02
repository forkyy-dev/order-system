package com.ordersystem.stock.ui.dto;

import com.ordersystem.stock.application.dto.ModifyStockDto;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class StockModifyRequest {
    private final String stockName;
    private final Double price;
    private final Integer quantity;
    private final Long categoryId;

    public ModifyStockDto toStockModifyDto(Long stockId) {
        return new ModifyStockDto(stockId, stockName, price, quantity, categoryId);
    }
}
