package com.ordersystem.order.exception;

import com.ordersystem.common.exception.ApplicationException;

public class OrderNotFoundException extends ApplicationException {

    public OrderNotFoundException(Long orderId) {
        super(String.format("해당 주문건을 찾을 수 없습니다. - 입력값: {%s}", orderId));
    }
}
