package com.ordersystem.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum StatusChangeReason {
    PRE_ORDER("PO","가주문"),
    CONFIRM_ORDER("CO","결제 및 주문 완료"),
    CANCEL_BY_USER("CBU","유저의 주문 취소"),
    CANCEL_BY_SOLD_OUT("CBSO", "품절로 인한 주문 취소"),
    ;

    private final String code;
    private final String description;

    public static StatusChangeReason getByCode(String code) {
        return Arrays.stream(StatusChangeReason.values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElseThrow();
    }
}
