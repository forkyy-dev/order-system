package com.ordersystem.order.application.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfirmOrderDto {

    private final Long userId;
    private final Long orderId;
    private final boolean paymentSuccess;

    public static ConfirmOrderDto from(Long orderId, boolean paymentSuccess) {
        return new ConfirmOrderDto(1L, orderId, paymentSuccess);
    }
}
