package com.ordersystem.order.ui.dto;

import com.ordersystem.order.application.dto.CreateOrderDto;
import com.ordersystem.order.application.dto.CreateOrderStockDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class CreateOrderRequest {
    @NotNull
    private final Long userId;
    @NotNull
    private final List<CreateOrderStockRequest> orderStocks;

    public CreateOrderDto toDto() {
        List<CreateOrderStockDto> orderStockDtos = this.orderStocks.stream()
                .map(dto -> CreateOrderStockDto.from(dto.getStockId(), dto.getQuantity()))
                .toList();
        return CreateOrderDto.from(userId, orderStockDtos);
    }
}
