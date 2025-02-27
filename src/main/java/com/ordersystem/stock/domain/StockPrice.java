package com.ordersystem.stock.domain;

import com.ordersystem.stock.exception.InvalidPricePointException;
import com.ordersystem.stock.exception.PriceOutOfRangeException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPrice {

    private static final Double HUNDRED = 100D;
    private Double price;

    public StockPrice(final Double price) {
        validatePrice(price);
        this.price = price;
    }

    void validatePrice(final Double price) {
        if (price <= 0) {
            throw new PriceOutOfRangeException(price);
        }

        if (price % HUNDRED != 0) {
            throw new InvalidPricePointException(price);
        }
    }
}
