package com.ordersystem.order.ui.dto;

import com.ordersystem.order.application.dto.OrderCreateDto;
import com.ordersystem.order.application.dto.OrderStockCreateDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class OrderCreateRequest {
    @NotNull
    private final Long userId;
    @NotNull
    private final List<OrderStockRequest> orderStocks;

    public OrderCreateDto toDto() {
        List<OrderStockCreateDto> orderStockDtos = this.orderStocks.stream()
                .map(dto -> OrderStockCreateDto.from(dto.getStockId(), dto.getQuantity()))
                .toList();
        return OrderCreateDto.from(userId, orderStockDtos);
    }
}
