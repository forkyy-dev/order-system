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
public class CreateOrderDto {
    private final Long userId;
    private final List<CreateOrderStockDto> orderStocks;

    public static CreateOrderDto from(Long userId, List<CreateOrderStockDto> orderStocks) {
        return new CreateOrderDto(userId, orderStocks);
    }

    public Map<Long, Integer> getIdQuantityPair() {
        return orderStocks.stream()
                .collect(Collectors.toMap(CreateOrderStockDto::getStockId, CreateOrderStockDto::getQuantity));
    }

    public Map<String, Integer> getKeyQuantityPair(String keyFormat) {
        return orderStocks.stream()
                .collect(Collectors.toMap((s) -> keyFormat + s.getStockId(), CreateOrderStockDto::getQuantity));
    }

    public Set<Long> getOrderStockIds() {
        return orderStocks.stream()
                .map(CreateOrderStockDto::getStockId)
                .collect(Collectors.toSet());
    }
}
