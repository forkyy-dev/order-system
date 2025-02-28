package com.ordersystem.stock.exception;

import com.ordersystem.common.exception.ApplicationException;

public class StockQuantityOutOfRangeException extends ApplicationException {
    public StockQuantityOutOfRangeException(int quantity) {
        super(String.format("상품의 개수는 0개보다 커야합니다. - 입력값: { %s }", quantity));
    }
}
