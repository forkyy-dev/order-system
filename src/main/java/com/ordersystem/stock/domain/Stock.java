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
    @AttributeOverrides({
            @AttributeOverride(name = "quantity", column = @Column(name = "current_quantity"))
    })
    private StockQuantity currentQuantity;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "quantity", column = @Column(name = "max_quantity"))
    })
    private StockQuantity maxQuantity;
    private Long categoryId;

    public Stock(final String name, final double price, final int currentQuantity, final int maxQuantity, final Long categoryId) {
        this.id = null;
        this.name = name;
        this.price = new StockPrice(price);
        this.currentQuantity = new StockQuantity(currentQuantity);
        this.maxQuantity = new StockQuantity(maxQuantity);
        this.categoryId = categoryId;
    }

    public Double getPrice() {
        return this.price.getPrice();
    }

    public Integer getCurrentQuantity() {
        return this.currentQuantity.getQuantity();
    }

    public Integer getMaxQuantity() {
        return this.maxQuantity.getQuantity();
    }
}
