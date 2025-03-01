package com.ordersystem.order.application.dto;

import com.ordersystem.common.dto.PageDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderInfoPaginationDto {
    private final List<OrderInfoDto> orders;
    private final PageDto pageInfo;

    public static OrderInfoPaginationDto empty(PageDto pageDto) {
        return new OrderInfoPaginationDto(List.of(), pageDto);
    }

    public static OrderInfoPaginationDto from(List<OrderInfoDto> orderInfoDtos, PageDto pageDto) {
        return new OrderInfoPaginationDto(orderInfoDtos, pageDto);
    }
}
