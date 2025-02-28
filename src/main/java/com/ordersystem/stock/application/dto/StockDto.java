package com.ordersystem.stock.application.dto;

import com.ordersystem.category.domain.Category;
import com.ordersystem.stock.domain.Stock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StockDto {
    private final long id;
    private final String name;
    private final double price;
    private final int currentQuantity;
    private final int maxQuantity;
    private final long categoryId;
    private final String categoryName;

    public static StockDto from(final Stock stock, final Category category) {
        return new StockDto(stock.getId(), stock.getName(), stock.getPrice(), stock.getCurrentQuantity(), stock.getMaxQuantity(), category.getId(), category.getName());
    }

}
