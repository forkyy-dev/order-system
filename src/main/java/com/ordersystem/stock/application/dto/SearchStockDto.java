package com.ordersystem.stock.application.dto;

import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
public class SearchStockDto {

    private final Long categoryId;
    private final String name;
    private final Pageable pageable;

    public SearchStockDto(Long categoryId, String name, Pageable pageable) {
        this.categoryId = categoryId;
        this.name = name.isEmpty() ? "" : name;
        this.pageable = pageable;
    }

    public SearchStockDto(Long categoryId, Pageable pageable) {
        this(categoryId, "", pageable);
    }
}
