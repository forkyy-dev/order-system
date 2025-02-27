package com.ordersystem.integration.stock;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.IntegrationTest;
import com.ordersystem.common.helper.FixtureBuilder;
import com.ordersystem.stock.application.StockService;
import com.ordersystem.stock.application.dto.StockCreateDto;
import com.ordersystem.stock.application.dto.StockDto;
import com.ordersystem.stock.application.dto.StockModifyDto;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.exception.DuplicatedStockException;
import com.ordersystem.stock.exception.StockNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

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
        stockRepository.save(FixtureBuilder.createSingleStock("상품1", category.getId()));
        StockCreateDto dto = new StockCreateDto("상품1", 10000, 20, category.getId());

        //when & then
        assertThatThrownBy(() -> stockService.create(dto))
                .isInstanceOf(DuplicatedStockException.class);
    }

    @Test
    @DisplayName("상품을 수정할 수 있다.")
    void modify_stock_success() {
        //given
        Stock stock = stockRepository.save(FixtureBuilder.createSingleStock("상품1", category.getId()));

        StockModifyDto dto = new StockModifyDto(
                stock.getId(),
                "의자2",
                stock.getPrice() - 1000,
                stock.getCurrentQuantity(),
                stock.getMaxQuantity(),
                category.getId()
        );

        //when
        StockDto result = stockService.modify(dto);

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
    @DisplayName("해당 상품이 존재하지 않을 경우 예외를 반환한다.")
    void modify_stock_fail_by_not_found() {
        //given
        StockModifyDto dto = new StockModifyDto(1L, "의자2", 9000, 10, 20, category.getId());

        //when & then
        assertThatThrownBy(() -> stockService.modify(dto))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @DisplayName("상품을 삭제할 수 있다.")
    void delete_stock_success() {
        //given
        Stock stock = stockRepository.save(FixtureBuilder.createSingleStock("상품1", category.getId()));

        //when
        stockService.delete(stock.getId());
        Optional<Stock> result = stockRepository.findById(stock.getId());

        //then
        assertThat(result).isEmpty();
    }
}



































