package com.ordersystem.category.ui.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CategoryCreateRequest {
    private final String categoryName;
}
