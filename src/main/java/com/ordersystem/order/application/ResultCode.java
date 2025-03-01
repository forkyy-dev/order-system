package com.ordersystem.order.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ResultCode {
    SUCCESS(1L),
    FAIL(-1L);

    private final Long statusCode;

    public static ResultCode from(Long statusCode) {
        return Arrays.stream(ResultCode.values())
                .filter(c -> c.statusCode.equals(statusCode))
                .findFirst()
                .orElseThrow();
    }

    public boolean isSuccess() {
        return this.equals(SUCCESS);
    }
}
