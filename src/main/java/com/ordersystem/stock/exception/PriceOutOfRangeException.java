package com.ordersystem.stock.exception;

import com.ordersystem.common.exception.ApplicationException;

public class PriceOutOfRangeException extends ApplicationException {
    public PriceOutOfRangeException(double price) {
        super(String.format("상품의 가격은 0원보다 커야합니다. - 입력값: { %s }", price));
    }
}
