package com.ordersystem.order.application.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCreateDto {
    private final Long userId;
    private final List<OrderStockCreateDto> orderStocks;

    public static OrderCreateDto from(Long userId, List<OrderStockCreateDto> orderStocks) {
        return new OrderCreateDto(userId, orderStocks);
    }

    public Map<Long, Integer> getIdQuantityPair() {
        return orderStocks.stream()
                .collect(Collectors.toMap(OrderStockCreateDto::getStockId, OrderStockCreateDto::getQuantity));
    }

    public Map<String, Integer> getKeyQuantityPair(String keyFormat) {
        return orderStocks.stream()
                .collect(Collectors.toMap((s) -> keyFormat + s.getStockId(), OrderStockCreateDto::getQuantity));
    }

    public Set<Long> getOrderStockIds() {
        return orderStocks.stream()
                .map(OrderStockCreateDto::getStockId)
                .collect(Collectors.toSet());
    }
}
