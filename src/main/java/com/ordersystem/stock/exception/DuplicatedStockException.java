package com.ordersystem.stock.exception;

import com.ordersystem.common.exception.ApplicationException;

public class DuplicatedStockException extends ApplicationException {
    public DuplicatedStockException(String name) {
        super(String.format("해당 카테고리에 동일한 이름의 상품이 존재합니다. - 입력값: { %s }", name));
    }
}
