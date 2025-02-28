package com.ordersystem.stock.ui.dto;

import com.ordersystem.stock.application.dto.StockModifyDto;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class StockModifyRequest {
    private final String stockName;
    private final Double price;
    private final Integer currentQuantity;
    private final Integer maxQuantity;
    private final Long categoryId;

    public StockModifyDto toStockModifyDto(Long stockId) {
        return new StockModifyDto(stockId, stockName, price, currentQuantity, maxQuantity, categoryId);
    }
}
