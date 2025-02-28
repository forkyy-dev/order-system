package com.ordersystem.category.application;

import com.ordersystem.category.application.dto.CategoryDto;
import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.category.exception.CategoryNotFoundException;
import com.ordersystem.category.exception.DuplicatedCategoryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDto create(String categoryName) {
        if (categoryRepository.existsByName(categoryName)) {
            throw new DuplicatedCategoryException(categoryName);
        }

        Category category = categoryRepository.save(new Category(categoryName));
        return CategoryDto.from(category);
    }

    @Transactional
    public CategoryDto modify(Long categoryId, String categoryName) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        category.update(categoryName);
        return CategoryDto.from(category);
    }


    @Transactional
    public void delete(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        categoryRepository.delete(category);
    }
}

