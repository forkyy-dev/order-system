package com.ordersystem.integration.order;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.IntegrationTest;
import com.ordersystem.common.helper.FixtureBuilder;
import com.ordersystem.order.application.OrderService;
import com.ordersystem.order.application.dto.OrderInfoDto;
import com.ordersystem.order.application.dto.OrderInfoPaginationDto;
import com.ordersystem.order.application.dto.OrderStockInfoDto;
import com.ordersystem.order.domain.*;
import com.ordersystem.order.exception.OrderNotFoundException;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("[Integration] 주문 조회 통합 테스트")
@IntegrationTest
class OrderReadServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderStockRepository orderStockRepository;

    private Category category;
    private List<Stock> stocks;

    @BeforeEach
    void setData() {
        stockRepository.deleteAll();
        categoryRepository.deleteAll();
        orderRepository.deleteAll();
        orderStockRepository.deleteAll();

        stocks = new ArrayList<>();
        category = categoryRepository.save(new Category("카테고리1"));
        setStockData();
    }

    @Test
    @DisplayName("사용자는 자신의 주문 목록을 조회할 수 있다.")
    void search_multi_order_success() {
        //given
        Long userId = 1L;
        Order order1 = orderRepository.save(new Order(30000D, OrderStatus.CONFIRM, userId));
        orderStockRepository.saveAll(
                List.of(new OrderStock(2, 20000D, stocks.get(2).getId(), order1.getId()),
                        new OrderStock(1, 10000D, stocks.get(3).getId(), order1.getId()))
        );
        Order order2 = orderRepository.save(new Order(20000D, OrderStatus.PRE, userId));
        orderStockRepository.saveAll(
                List.of(new OrderStock(1, 10000D, stocks.get(4).getId(), order2.getId()),
                        new OrderStock(1, 10000D, stocks.get(5).getId(), order2.getId()))
        );
        List<Order> orders = List.of(order2, order1);

        //when
        OrderInfoPaginationDto orderDtos = orderService.getAllOrderInfoByPagination(userId, Pageable.ofSize(10));

        //then
        assertAll(() -> {
            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                OrderInfoDto dto = orderDtos.getOrders().get(i);

                assertThat(order.getOrderNo()).isEqualTo(dto.getOrderNo());
                assertThat(order.getTotalPrice()).isEqualTo(dto.getTotalPrice());
                assertThat(order.getStatus()).isEqualTo(dto.getOrderStatus());
            }
            assertThat(orderDtos.getPageInfo().getSize()).isEqualTo(10);
            assertThat(orderDtos.getPageInfo().getPageNumber()).isEqualTo(0);
            assertThat(orderDtos.getPageInfo().isLast()).isTrue();
        });
    }

    @Test
    @DisplayName("주문 목록 조회 시 주문 정보가 없으면 빈 배열을 반환한다.")
    void search_multi_order_fail_by_not_found() {
        //when
        OrderInfoPaginationDto orderDtos = orderService.getAllOrderInfoByPagination(1L, Pageable.ofSize(10));

        //then
        assertThat(orderDtos.getOrders()).isEmpty();
    }

    @Test
    @DisplayName("사용자는 주문에 대한 상세 정보 조회를 할 수 있다.")
    void search_single_order_success() {
        //given
        Long userId = 1L;
        Order order = orderRepository.save(new Order(30000D, OrderStatus.CONFIRM, userId));
        List<OrderStock> orderStocks = orderStockRepository.saveAll(
                List.of(new OrderStock(2, 20000D, stocks.get(2).getId(), order.getId()),
                        new OrderStock(1, 10000D, stocks.get(3).getId(), order.getId()))
        );
        //when
        OrderInfoDto singleOrderInfo = orderService.getSingleOrderInfo(order.getId());

        //then
        assertAll(() -> {
            assertThat(order.getOrderNo()).isEqualTo(singleOrderInfo.getOrderNo());
            assertThat(order.getTotalPrice()).isEqualTo(singleOrderInfo.getTotalPrice());
            assertThat(order.getStatus()).isEqualTo(singleOrderInfo.getOrderStatus());

            for (int i = 0; i < orderStocks.size(); i++) {
                OrderStock orderStock = orderStocks.get(i);
                OrderStockInfoDto dto = singleOrderInfo.getStocks().get(i);

                assertThat(orderStock.getStockId()).isEqualTo(dto.getStockId());
                assertThat(orderStock.getOriginalPrice()).isEqualTo(dto.getOriginalPrice());
                assertThat(orderStock.getTotalPrice()).isEqualTo(dto.getTotalPrice());
                assertThat(orderStock.getQuantity()).isEqualTo(dto.getQuantity());
            }
        });
    }

    @Test
    @DisplayName("단일 조회 시 주문 정보가 없으면 예외를 반환한다.")
    void search_single_order_fail_by_not_found() {
        assertThatThrownBy(() -> orderService.getSingleOrderInfo(100000L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private void setStockData() {
        for (int i = 1; i <= 10; i++) {
            stocks.add(stockRepository.save(FixtureBuilder.createSingleStock("상품" + i, category.getId())));
        }
    }

}
