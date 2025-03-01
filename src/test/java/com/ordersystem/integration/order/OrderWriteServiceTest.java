package com.ordersystem.integration.order;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.IntegrationTest;
import com.ordersystem.common.config.RedisEmbeddedConfig;
import com.ordersystem.common.helper.FixtureBuilder;
import com.ordersystem.order.application.OrderService;
import com.ordersystem.order.application.dto.*;
import com.ordersystem.order.domain.*;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.exception.StockNotFoundException;
import com.ordersystem.stock.exception.StockSoldOutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Integration] 주문 생성, 취소 통합 테스트")
@Import({RedisEmbeddedConfig.class})
@IntegrationTest
class OrderWriteServiceTest {

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
    @Autowired
    private StringRedisTemplate redisTemplate;

    private Category category;
    private List<Stock> stocks;
    private Stock cacheStock;
    private Stock dbStock;
    private Stock soldOutStock;
    private Order order;

    @BeforeEach
    void setData() throws IOException {
        stockRepository.deleteAll();
        categoryRepository.deleteAll();

        stocks = new ArrayList<>();
        category = categoryRepository.save(new Category("카테고리1"));
        //상품 데이터 세팅
        setStockData();
        setRedisData();
        setOrderData();
    }

    @Test
    @DisplayName("최초 가주문을 생성할 수 있다.")
    void create_order_success() {
        //given
        int quantity = 3;
        double price = cacheStock.getPrice() * quantity;
        CreateOrderStockDto createOrderStockDto = CreateOrderStockDto.from(cacheStock.getId(), quantity);
        CreateOrderDto dto = CreateOrderDto.from(1L, List.of(createOrderStockDto));

        //when
        OrderDto order = orderService.createOrder(dto);

        //then
        assertThat(order.getOrderStocks().size()).isEqualTo(1);
        assertThat(order.getTotalPrice()).isEqualTo(price);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PRE);
    }

    @Test
    @DisplayName("품절 상품의 상품을 주문할 경우 예외를 반환한다.")
    void order_fail_by_sold_out() {
        //given
        int quantity = 3;
        CreateOrderStockDto createOrderStockDto = CreateOrderStockDto.from(soldOutStock.getId(), quantity);
        CreateOrderDto dto = CreateOrderDto.from(1L, List.of(createOrderStockDto));

        //when & then
        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(StockSoldOutException.class);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 주문할 경우 예외를 반환한다.")
    void order_fail_by_stock_not_found() {
        //given
        CreateOrderStockDto createOrderStockDto = CreateOrderStockDto.from(0L, 1);
        CreateOrderDto dto = CreateOrderDto.from(1L, List.of(createOrderStockDto));

        //when & then
        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @DisplayName("캐시 저장소에 데이터가 없더라도 올바른 주문이면 가주문을 생성한다.")
    void create_order_without_cache_success() {
        int quantity = 3;
        double price = dbStock.getPrice() * quantity;
        CreateOrderStockDto createOrderStockDto = CreateOrderStockDto.from(dbStock.getId(), quantity);
        CreateOrderDto dto = CreateOrderDto.from(1L, List.of(createOrderStockDto));

        //when
        OrderDto order = orderService.createOrder(dto);

        //then
        assertThat(order.getOrderStocks().size()).isEqualTo(1);
        assertThat(order.getTotalPrice()).isEqualTo(price);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PRE);
    }

    @Test
    @DisplayName("캐시 저장소에 존재하지 않는 상품을 조회했을 경우 이벤트를 발행한다.")
    void issue_update_cache_event() {
        //given
        int quantity = 3;
        CreateOrderStockDto createOrderStockDto = CreateOrderStockDto.from(dbStock.getId(), quantity);
        CreateOrderDto dto = CreateOrderDto.from(1L, List.of(createOrderStockDto));

        //when
        orderService.createOrder(dto);

        //then
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        int remainingStock = Integer.parseInt(ops.get("stock:" + dbStock.getId()));
        assertThat(remainingStock).isEqualTo(20);
    }

    @Test
    @DisplayName("주문 확정 요청 시 결제에 성공했으면 주문의 상태를 완료로 변경한다.")
    void confirm_order_success() {
        //given
        ConfirmOrderDto dto = ConfirmOrderDto.from(order.getId(), true);

        //when
        OrderDto orderDto = orderService.confirmOrder(dto);

        //then
        assertThat(orderDto.getOrderStatus()).isEqualTo(OrderStatus.CONFIRM);
    }

    @Test
    @DisplayName("주문 확정에 성공하면 DB상의 상품 재고의 수량을 수정한다.")
    void update_db_stock_when_confirm_success() {
        //given
        ConfirmOrderDto dto = ConfirmOrderDto.from(order.getId(), true);

        //when
        OrderDto result = orderService.confirmOrder(dto);

        List<Long> stockIds = result.getOrderStocks()
                .stream()
                .map(OrderStockDto::getStockId)
                .toList();

        //기본적으로 상품들은 20개의 재고를 가지고 있음.
        int stocksTotalQuantity = stockRepository.findAllById(stockIds)
                .stream().mapToInt(Stock::getQuantity)
                .sum();

        //then
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CONFIRM);
        assertThat(stocksTotalQuantity).isEqualTo(37);
    }

    @Test
    @DisplayName("주문 취소 요청 시 가주문일 경우 주문의 상태를 취소로 변경한다.")
    void cancel_pre_order_success() {
        //given
        Order order = orderRepository.save(new Order(30000D, 1L));
        orderStockRepository.save(new OrderStock(1, 10000D, dbStock.getId(), order.getId()));

        //when
        OrderDto result = orderService.cancelOrder(order.getId());

        //then
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCEL);
    }

    @Test
    @DisplayName("주문 취소 요청 시 주문완료인 주문일 경우 상태를 취소로 변경하고 DB의 재고를 수정한다.")
    void cancel_order_success() {
        //given
        Stock stock = stockRepository.save(FixtureBuilder.createSingleStock("테스트 상품입니다.", category.getId()));
        int beforeQuantity = stock.getQuantity();
        Order order = orderRepository.save(new Order(30000D, OrderStatus.CONFIRM,1L));
        OrderStock orderStock = orderStockRepository.save(new OrderStock(1, 10000D, stock.getId(), order.getId()));

        //when
        orderService.cancelOrder(order.getId());
        int afterQuantity = stockRepository.findById(stock.getId()).get().getQuantity();

        //then
        assertThat(beforeQuantity + orderStock.getQuantity()).isEqualTo(afterQuantity);
    }

    private void setRedisData() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        ops.set("stock:" + cacheStock.getId(), "10");
        ops.set("stock:" + soldOutStock.getId(), "0");
    }

    private void setStockData() {
        for (int i = 1; i <= 5; i++) {
            stocks.add(stockRepository.save(FixtureBuilder.createSingleStock("상품" + i, category.getId())));
        }
        cacheStock = stocks.get(0);
        dbStock = stocks.get(1);
        //품절 상품 데이터 세팅
        soldOutStock = stockRepository.save(new Stock("품절 상품1", 10000, 0, category.getId()));
    }


    private void setOrderData() {
        order = orderRepository.save(new Order(30000D, 1L));
        orderStockRepository.saveAll(
                List.of(new OrderStock(2, 20000D, stocks.get(2).getId(), order.getId()),
                        new OrderStock(1, 10000D, stocks.get(3).getId(), order.getId()))
        );
    }
}























