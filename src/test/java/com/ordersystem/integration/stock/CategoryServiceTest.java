package com.ordersystem.integration.stock;

import com.ordersystem.category.application.CategoryService;
import com.ordersystem.category.application.dto.CategoryDto;
import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.category.exception.CategoryNotFoundException;
import com.ordersystem.category.exception.DuplicatedCategoryException;
import com.ordersystem.common.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Integration] 카테고리 통합 테스트")
@IntegrationTest
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setData() {
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("카테고리를 생성할 수 있다.")
    void create_category_success() {
        //given
        String categoryName = "카테고리1";

        //when
        CategoryDto result = categoryService.create(categoryName);

        //then
        assertThat(result.getCategoryName()).isEqualTo(categoryName);
    }

    @Test
    @DisplayName("중복된 카테고리를 생성할 경우 예외를 반환한다.")
    void create_category_fail_by_duplicated_name() {
        //given
        String categoryName = "카테고리1";
        categoryRepository.save(new Category(categoryName));

        //when & then
        assertThatThrownBy(() -> categoryService.create(categoryName))
                .isInstanceOf(DuplicatedCategoryException.class);
    }

    @Test
    @DisplayName("카테고리를 수정할 수 있다.")
    void modify_category_success() {
        //given
        Category existCategory = categoryRepository.save(new Category("카테고리1"));
        String newCategoryName = "카테고리2";

        //when
        CategoryDto result = categoryService.modify(existCategory.getId(), newCategoryName);

        //then
        assertThat(result.getCategoryName()).isEqualTo(newCategoryName);
    }

    @Test
    @DisplayName("해당 카테고리가 존재하지 않을 경우 예외를 반환한다.")
    void modify_category_fail_by_not_found() {
        assertThatThrownBy(() -> categoryService.modify(1L, "카테고리1"))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    @DisplayName("카테고리를 삭제할 수 있다.")
    void delete_category_success() {
        //given
        Category category = categoryRepository.save(new Category("카테고리1"));

        //when
        categoryService.delete(category.getId());
        Optional<Category> result = categoryRepository.findById(category.getId());

        //then
        assertThat(result).isEmpty();
    }

}
