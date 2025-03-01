package com.ordersystem.integration.order;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.IntegrationTest;
import com.ordersystem.common.helper.FixtureBuilder;
import com.ordersystem.order.application.OrderService;
import com.ordersystem.order.application.dto.OrderCreateDto;
import com.ordersystem.order.application.dto.OrderDto;
import com.ordersystem.order.application.dto.OrderStockDto;
import com.ordersystem.order.domain.OrderStatus;
import com.ordersystem.order.exception.OrderQuantityExceedException;
import com.ordersystem.stock.exception.StockNotFoundException;
import com.ordersystem.stock.exception.StockSoldOutException;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Integration] 주문 생성, 취소 통합 테스트")
@IntegrationTest
class OrderWriteServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    Category category;
    List<Stock> stocks;
    Stock soldOutStock;

    @BeforeEach
    void setData() {
        stockRepository.deleteAll();
        categoryRepository.deleteAll();

        stocks = new ArrayList<>();
        category = categoryRepository.save(new Category("카테고리1"));
        //상품 데이터 세팅
        for (int i = 1; i <= 20; i++) {
            stocks.add(stockRepository.save(FixtureBuilder.createSingleStock("상품" + i, category.getId())));
        }
        //품절 상품 데이터 세팅
        soldOutStock = stockRepository.save(new Stock("품절 상품1", 10000, 0, category.getId()));
    }

    @Test
    @DisplayName("주문을 생성할 수 있다.")
    void create_order_success() {
        //given
        Stock stock = stocks.get(0);
        int quantity = 3;
        double price = stock.getPrice() * quantity;
        OrderStockDto orderStockDto = new OrderStockDto(stock.getId(), quantity, price);
        OrderCreateDto dto = new OrderCreateDto(1L, List.of(orderStockDto));

        //when
        OrderDto order = orderService.createOrder(dto);

        //then
        assertThat(order.getOrderStocks().size()).isEqualTo(1);
        assertThat(order.getTotalPrice()).isEqualTo(price);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PRE);
    }

    @Test
    @DisplayName("품절 상품의 상품을 주문할 경우 예외를 반환한다.")
    void order_fail_by_sold_out() {
        //given
        int quantity = 3;
        double price = soldOutStock.getPrice() * quantity;
        OrderStockDto orderStockDto = new OrderStockDto(soldOutStock.getId(), quantity, price);
        OrderCreateDto dto = new OrderCreateDto(1L, List.of(orderStockDto));

        //when & then
        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(StockSoldOutException.class);
    }

    @Test
    @DisplayName("재고보다 많은 수량을 주문할 경우 예외를 반환한다.")
    void order_fail_by_over_amount() {
        //given
        Stock stock = stocks.get(0);
        int quantity = stock.getQuantity() + 1;
        double price = soldOutStock.getPrice() * quantity;
        OrderStockDto orderStockDto = new OrderStockDto(soldOutStock.getId(), quantity, price);
        OrderCreateDto dto = new OrderCreateDto(1L, List.of(orderStockDto));

        //when & then
        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(OrderQuantityExceedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 주문할 경우 예외를 반환한다.")
    void order_fail_by_stock_not_found() {
        //given
        OrderStockDto orderStockDto = new OrderStockDto(0L, 1, 1000);
        OrderCreateDto dto = new OrderCreateDto(1L, List.of(orderStockDto));

        //when & then
        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(StockNotFoundException.class);
    }

}
