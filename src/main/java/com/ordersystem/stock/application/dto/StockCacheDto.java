package com.ordersystem.stock.application.dto;

import com.ordersystem.stock.domain.Stock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StockCacheDto {
    private final Long id;
    private final Integer quantity;

    public static StockCacheDto from(Stock stock) {
        return new StockCacheDto(stock.getId(), stock.getQuantity());
    }
}
