package com.ordersystem.stock.domain;

import com.ordersystem.stock.exception.StockQuantityOutOfRangeException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockQuantity {

    private Integer quantity;

    public StockQuantity(final int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    private void validateQuantity(final int quantity) {
        if (quantity < 0) {
            throw new StockQuantityOutOfRangeException(quantity);
        }
    }
}
