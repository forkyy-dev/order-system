package com.ordersystem.stock.exception;

import com.ordersystem.common.exception.ApplicationException;

public class InvalidPricePointException extends ApplicationException {
    public InvalidPricePointException(double price) {
        super(String.format("상품의 가격 단위는 100원 단위여야 합니다. - 입력값: { %s }", price));
    }
}
