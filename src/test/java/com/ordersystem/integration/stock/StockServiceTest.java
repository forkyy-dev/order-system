package com.ordersystem.integration.stock;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.IntegrationTest;
import com.ordersystem.common.helper.FixtureBuilder;
import com.ordersystem.config.RedisTestConfig;
import com.ordersystem.stock.application.StockService;
import com.ordersystem.stock.application.dto.*;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.exception.DuplicatedStockException;
import com.ordersystem.stock.exception.StockNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Import({RedisTestConfig.class})
@DisplayName("[Integration] 상품 통합 테스트")
@IntegrationTest
class StockServiceTest {

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
        CreateStockDto dto = new CreateStockDto("상품1", 10000, 20, category.getId());

        //when
        StockDto result = stockService.create(dto);

        //then
        assertAll(() -> {
            assertThat(result.getName()).isEqualTo(dto.getName());
            assertThat(result.getPrice()).isEqualTo(dto.getPrice());
            assertThat(result.getQuantity()).isEqualTo(dto.getQuantity());
            assertThat(result.getCategoryId()).isEqualTo(dto.getCategoryId());
        });
    }

    @Test
    @DisplayName("동일한 카테고리 내에 동일한 이름의 상품이 존재할 경우 예외를 반환한다.")
    void create_stock_fail_by_duplicated_name() {
        //given
        stockRepository.save(FixtureBuilder.createSingleStock("상품1", category.getId()));
        CreateStockDto dto = new CreateStockDto("상품1", 10000, 20, category.getId());

        //when & then
        assertThatThrownBy(() -> stockService.create(dto))
                .isInstanceOf(DuplicatedStockException.class);
    }

    @Test
    @DisplayName("상품을 수정할 수 있다.")
    void modify_stock_success() {
        //given
        Stock stock = stockRepository.save(FixtureBuilder.createSingleStock("상품1", category.getId()));

        ModifyStockDto dto = new ModifyStockDto(
                stock.getId(),
                "의자2",
                stock.getPrice() - 1000,
                stock.getQuantity(),
                category.getId()
        );

        //when
        StockDto result = stockService.modify(dto);

        //then
        assertAll(() -> {
            assertThat(result.getName()).isEqualTo(dto.getName());
            assertThat(result.getPrice()).isEqualTo(dto.getPrice());
            assertThat(result.getQuantity()).isEqualTo(dto.getQuantity());
            assertThat(result.getCategoryId()).isEqualTo(dto.getCategoryId());
        });
    }

    @Test
    @DisplayName("해당 상품이 존재하지 않을 경우 예외를 반환한다.")
    void modify_stock_fail_by_not_found() {
        //given
        ModifyStockDto dto = new ModifyStockDto(1L, "의자2", 9000, 20, category.getId());

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

    @Test
    @DisplayName("카테고리 번호와 상품명으로 상품 목록을 조회할 수 있다.")
    void read_stocks_by_stock_name_success() {
        //given
        Stock stock = stockRepository.save(FixtureBuilder.createSingleStock("상품1", category.getId()));
        SearchStockDto dto = new SearchStockDto(category.getId(), stock.getName(), PageRequest.of(0, 10));

        //when
        StockPaginationDto result = stockService.search(dto);

        //then
        assertThat(result.getStocks().get(0).getId()).isEqualTo(stock.getId());
        assertThat(result.getStocks().get(0).getName()).isEqualTo(stock.getName());
    }

    @Test
    @DisplayName("카테고리에 해당하는 상품 목록을 조회할 수 있다.")
    void read_stocks_by_category_success() {
        //given
        for (int i = 0; i < 10; i++) {
            stockRepository.save(FixtureBuilder.createSingleStock("상품" + i, category.getId()));
        }
        SearchStockDto dto = new SearchStockDto(category.getId(), PageRequest.of(0, 10));

        //when

        StockPaginationDto result = stockService.search(dto);

        //then
        assertThat(result.getPageInfo().getSize()).isEqualTo(10);
        assertThat(result.getPageInfo().getPageNumber()).isEqualTo(0);
    }
}



































