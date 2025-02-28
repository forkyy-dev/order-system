package com.ordersystem.category.application.dto;

import com.ordersystem.category.domain.Category;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CategoryDto {
    private final Long categoryId;
    private final String categoryName;

    public static CategoryDto from(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
