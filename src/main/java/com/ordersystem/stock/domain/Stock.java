package com.ordersystem.stock.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @Embedded
    private StockPrice price;
    @Embedded
    private StockQuantity quantity;
    private Long categoryId;

    public Stock(final Long id, final String name, final double price, final int quantity, final Long categoryId) {
        this.id = id;
        this.name = name;
        this.price = new StockPrice(price);
        this.quantity = new StockQuantity(quantity);
        this.categoryId = categoryId;
    }

    public Stock(final String name, final double price, final int currentQuantity, final Long categoryId) {
        this(null, name, price, currentQuantity, categoryId);
    }

    public Double getPrice() {
        return this.price.getPrice();
    }

    public Integer getQuantity() {
        return this.quantity.getQuantity();
    }

    public void updateInfo(String name, double price, int quantity, long categoryId) {
        this.name = name;
        this.price = new StockPrice(price);
        this.quantity = new StockQuantity(quantity);
        this.categoryId = categoryId;
    }
}
