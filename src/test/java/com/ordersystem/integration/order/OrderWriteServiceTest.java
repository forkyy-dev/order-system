package com.ordersystem.integration.order;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.IntegrationTest;
import com.ordersystem.common.config.RedisEmbeddedConfig;
import com.ordersystem.common.helper.FixtureBuilder;
import com.ordersystem.order.application.OrderService;
import com.ordersystem.order.application.dto.OrderCreateDto;
import com.ordersystem.order.application.dto.OrderDto;
import com.ordersystem.order.application.dto.OrderStockCreateDto;
import com.ordersystem.order.domain.OrderStatus;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.exception.StockNotFoundException;
import com.ordersystem.stock.exception.StockSoldOutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Integration] 주문 생성, 취소 통합 테스트")
@Import({RedisEmbeddedConfig.class})
@IntegrationTest
class OrderWriteServiceTest {

    private RedisServer redisServer;
    @Autowired
    private OrderService orderService;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private Category category;
    private List<Stock> stocks;
    private Stock cacheStock;
    private Stock dbStock;
    private Stock soldOutStock;

    @BeforeEach
    void setData() throws IOException {
        redisServer = new RedisServer(find_available_port());
        redisServer.start();
        stockRepository.deleteAll();
        categoryRepository.deleteAll();

        stocks = new ArrayList<>();
        category = categoryRepository.save(new Category("카테고리1"));
        //상품 데이터 세팅
        for (int i = 1; i <= 5; i++) {
            stocks.add(stockRepository.save(FixtureBuilder.createSingleStock("상품" + i, category.getId())));
        }
        cacheStock = stocks.get(0);
        dbStock = stocks.get(1);
        //품절 상품 데이터 세팅
        soldOutStock = stockRepository.save(new Stock("품절 상품1", 10000, 0, category.getId()));

        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        ops.set("stock:" + cacheStock.getId(), "10");
        ops.set("stock:" + soldOutStock.getId(), "0");
    }

    @AfterEach
    void stop_redis() throws IOException {
        redisServer.stop();
    }

    @Test
    @DisplayName("주문을 생성할 수 있다.")
    void create_order_success() {
        //given
        int quantity = 3;
        double price = cacheStock.getPrice() * quantity;
        OrderStockCreateDto orderStockCreateDto = OrderStockCreateDto.from(cacheStock.getId(), quantity);
        OrderCreateDto dto = OrderCreateDto.from(1L, List.of(orderStockCreateDto));

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
        OrderStockCreateDto orderStockCreateDto = OrderStockCreateDto.from(soldOutStock.getId(), quantity);
        OrderCreateDto dto = OrderCreateDto.from(1L, List.of(orderStockCreateDto));

        //when & then
        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(StockSoldOutException.class);
    }


    @Test
    @DisplayName("존재하지 않는 상품을 주문할 경우 예외를 반환한다.")
    void order_fail_by_stock_not_found() {
        //given
        OrderStockCreateDto orderStockCreateDto = OrderStockCreateDto.from(0L, 1);
        OrderCreateDto dto = OrderCreateDto.from(1L, List.of(orderStockCreateDto));

        //when & then
        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @DisplayName("캐시 저장소에 데이터가 없더라도 올바른 주문이면 주문을 생성한다.")
    void create_order_without_cache_success() {
        int quantity = 3;
        double price = dbStock.getPrice() * quantity;
        OrderStockCreateDto orderStockCreateDto = OrderStockCreateDto.from(dbStock.getId(), quantity);
        OrderCreateDto dto = OrderCreateDto.from(1L, List.of(orderStockCreateDto));

        //when
        OrderDto order = orderService.createOrder(dto);

        //then
        assertThat(order.getOrderStocks().size()).isEqualTo(1);
        assertThat(order.getTotalPrice()).isEqualTo(price);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PRE);
    }

    @Test
    @DisplayName("캐시 저장소에 존재하지 않는 상품을 조회했을 경우 이벤트를 발행한다.")
    void issue_update_cache_event() {
        //given
        int quantity = 3;
        OrderStockCreateDto orderStockCreateDto = OrderStockCreateDto.from(dbStock.getId(), quantity);
        OrderCreateDto dto = OrderCreateDto.from(1L, List.of(orderStockCreateDto));

        //when
        orderService.createOrder(dto);

        //then
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        int remainingStock = Integer.parseInt(ops.get("stock:" + dbStock.getId()));
        assertThat(remainingStock).isEqualTo(20);
    }

    private int find_available_port() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
