package com.ordersystem.order.ui.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConfirmOrderRequest {
    private final Long userId;
    private final Boolean paymentSuccess;

}
