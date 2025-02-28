package com.ordersystem.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PageDto {
    private final int size;
    private final int pageNumber;
    private final boolean last;

    public static PageDto of(int size, int pageNumber, boolean last) {
        return new PageDto(size, pageNumber, last);
    }
}
