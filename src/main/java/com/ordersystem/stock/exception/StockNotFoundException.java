package com.ordersystem.stock.exception;

import com.ordersystem.common.exception.ApplicationException;

public class StockNotFoundException extends ApplicationException {
    public StockNotFoundException(Long id) {
        super(String.format("해당 상품을 찾을 수 없습니다. - 입력값: { %s }", id));
    }
}
