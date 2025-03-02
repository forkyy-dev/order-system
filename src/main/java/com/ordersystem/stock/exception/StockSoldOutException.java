package com.ordersystem.stock.exception;

import com.ordersystem.common.exception.ApplicationException;

import java.util.Set;

public class StockSoldOutException extends ApplicationException {

    public StockSoldOutException(Set<Long> stockIds) {
        super(String.format("품절된 상품입니다. - 입력값: {id: %s}", stockIds.toString()));
    }

    public StockSoldOutException() {
        super("품절된 상품이 존재합니다.");
    }
}
