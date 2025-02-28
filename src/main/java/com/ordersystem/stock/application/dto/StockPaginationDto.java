package com.ordersystem.stock.application.dto;

import com.ordersystem.common.dto.PageDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class StockPaginationDto {
    private final List<StockDto> stocks;
    private final PageDto pageInfo;
}
