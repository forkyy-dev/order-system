package com.ordersystem.order.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

@Getter
@Embeddable
public class OrderNumber {
    private static final String ORDER_NUMBER_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private String orderNumber;

    public OrderNumber() {
        this.orderNumber = createOrderNo();
    }

    private static String createOrderNo() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(ORDER_NUMBER_LETTERS.charAt(random.nextInt(ORDER_NUMBER_LETTERS.length() - 1)));
        }
        sb.append(String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)), 0, 9);
        return sb.toString();
    }
}
