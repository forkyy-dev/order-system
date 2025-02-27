package com.ordersystem.integration.stock;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.IntegrationTest;
import com.ordersystem.stock.application.StockService;
import com.ordersystem.stock.application.dto.StockCreateDto;
import com.ordersystem.stock.application.dto.StockDto;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.exception.DuplicatedStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("[통합테스트] 상품")
@IntegrationTest
public class StockServiceTest {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    @Autowired
    private StockService stockService;

    @BeforeEach
    void cleanRepository() {
        stockRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(new Category("카테고리1"));
    }

    @Test
    @DisplayName("상품을 등록할 수 있다.")
    void create_stock_success() {
        //given
        StockCreateDto dto = new StockCreateDto("상품1", 10000, 20, category.getId());

        //when
        StockDto result = stockService.create(dto);

        //then
        assertAll(() -> {
            assertThat(result.getName()).isEqualTo(dto.getName());
            assertThat(result.getPrice()).isEqualTo(dto.getPrice());
            assertThat(result.getCurrentQuantity()).isEqualTo(dto.getMaxQuantity());
            assertThat(result.getMaxQuantity()).isEqualTo(dto.getMaxQuantity());
            assertThat(result.getCategoryId()).isEqualTo(dto.getCategoryId());
        });
    }

    @Test
    @DisplayName("동일한 카테고리 내에 동일한 이름의 상품이 존재할 경우 예외를 반환한다.")
    void create_stock_fail_by_duplicated_name() {
        //given
        stockRepository.save(new Stock("상품1", 10000, 20, 20, category.getId()));
        StockCreateDto dto = new StockCreateDto("상품1", 10000, 20, category.getId());

        //when & then
        assertThatThrownBy(() -> stockService.create(dto))
                .isInstanceOf(DuplicatedStockException.class);
    }

}



































