package com.ordersystem.category.ui;

import com.ordersystem.category.application.CategoryService;
import com.ordersystem.category.application.dto.CategoryDto;
import com.ordersystem.category.ui.dto.CategoryCreateRequest;
import com.ordersystem.category.ui.dto.CategoryModifyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/category")
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody CategoryCreateRequest request) {
        CategoryDto result = categoryService.create(request.getCategoryName());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> modify(@PathVariable Long categoryId, @RequestBody CategoryModifyRequest request) {
        CategoryDto result = categoryService.modify(categoryId, request.getCategoryName());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.ok(null);
    }
}
