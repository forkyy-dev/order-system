package com.ordersystem.order.exception;

import com.ordersystem.common.exception.ApplicationException;

public class AlreadyCanceledException extends ApplicationException {
    public AlreadyCanceledException(Long orderId) {
        super(String.format("이미 취소된 주문입니다. - 입력값: {%s}", orderId));
    }
}
