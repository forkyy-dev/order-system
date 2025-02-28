package com.ordersystem.common.helper;

import com.ordersystem.stock.domain.Stock;

public class FixtureBuilder {

    private static final double DEFAULT_PRICE = 10000;
    private static final int DEFAULT_QUANTITY = 20;

    public static Stock createSingleStock(String name, Long categoryId) {
        return new Stock(name, DEFAULT_PRICE, DEFAULT_QUANTITY, categoryId);
    }

}
