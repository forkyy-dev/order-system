package com.ordersystem.e2e.order;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.ControllerTest;
import com.ordersystem.common.helper.FixtureBuilder;
import com.ordersystem.order.domain.*;
import com.ordersystem.order.ui.dto.ConfirmOrderRequest;
import com.ordersystem.order.ui.dto.CreateOrderRequest;
import com.ordersystem.order.ui.dto.CreateOrderStockRequest;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@DisplayName("[RestDocs] 주문 API 테스트")
class OrderControllerTest extends ControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderStockRepository orderStockRepository;

    Category category;
    List<Stock> stocks;
    Stock stock;
    Order confirmedOrder;

    @BeforeEach
    void setData() {
        stockRepository.deleteAll();
        categoryRepository.deleteAll();
        orderRepository.deleteAll();
        orderStockRepository.deleteAll();

        stocks = new ArrayList<>();
        category = categoryRepository.save(new Category("카테고리1"));
        setStockData();
        setCacheData();
        setOrderData();
    }

    @Test
    @DisplayName("사용자는 가주문을 생성할 수 있다.")
    void create_pre_order_success() {
        Stock stock = stocks.get(0);
        int quantity = 2;
        double price = stock.getPrice() * quantity;
        CreateOrderStockRequest stockRequest = new CreateOrderStockRequest(stock.getId(), quantity, price);
        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(stockRequest));

        given(this.spec)
                .filter(
                        document("order-create",
                                requestFieldsOrderDto(),
                                responseFieldsStockDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .post("/api/order")
        .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("사용자는 주문을 완료할 수 있다.")
    void confirm_order_success() {
        ConfirmOrderRequest request = new ConfirmOrderRequest(1L, true);

        given(this.spec)
                .filter(
                        document("order-confirm",
                                pathParameters(parameterWithName("orderId").description("orderID")),
                                requestFields(
                                        fieldWithPath("userId").description("유저 고유 ID").type(JsonFieldType.NUMBER),
                                        fieldWithPath("paymentSuccess").description("결제 성공 여부").type(JsonFieldType.BOOLEAN)
                                ),
                                responseFieldsStockDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .put("/api/order/{orderId}/confirm", confirmedOrder.getId())
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("orderStatus", equalTo(OrderStatus.CONFIRM.name()));
    }

    @Test
    @DisplayName("사용자는 주문을 취소할 수 있다.")
    void cancel_order_success() {
        given(this.spec)
                .filter(
                        document("order-cancel",
                                responseFieldsStockDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
        .when()
                .put("/api/order/{orderId}/cancel", confirmedOrder.getId())
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("orderStatus", equalTo(OrderStatus.CANCEL.name()));
    }

    @Test
    @DisplayName("사용자는 자신의 주문 목록을 조회할 수 있다.")
    void search_multi_order_success() {
        given(this.spec)
                .filter(
                        document("order-multi-search",
                                queryParameters(
                                        parameterWithName("userId").description("유저 고유 ID"),
                                        parameterWithName("pageNumber").description("페이지 번호")
                                ),
                                responseFieldsOrderInfoPaginationDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .queryParam("userId", 1L)
                .queryParam("pageNumber", 0)
        .when()
                .get("/api/orders")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("pageInfo.size", equalTo(1));
    }

    @Test
    @DisplayName("사용자는 주문에 대한 상세 정보 조회를 할 수 있다.")
    void search_single_order_success() {
        given(this.spec)
                .filter(
                        document("order-single-search",
                                queryParameters(parameterWithName("userId").description("유저 고유 ID")),
                                pathParameters(parameterWithName("orderId").description("주문 고유 ID")),
                                responseFieldsOrderInfoDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .queryParam("userId", 1L)
        .when()
                .get("/api/orders/{orderId}", confirmedOrder.getId())
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("stocks.size()", equalTo(2));
    }

    private void setCacheData() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("stock:" + stock.getId(), stock.getQuantity().toString());
    }

    private void setStockData() {
        for (int i = 1; i <= 20; i++) {
            stocks.add(stockRepository.save(FixtureBuilder.createSingleStock("상품" + i, category.getId())));
        }
        stock = stocks.get(0);
    }

    private void setOrderData() {
        confirmedOrder = orderRepository.save(new Order(30000D, OrderStatus.CONFIRM,1L));
        orderStockRepository.saveAll(
                List.of(new OrderStock(2, 20000D, stocks.get(2).getId(), confirmedOrder.getId()),
                        new OrderStock(1, 10000D, stocks.get(3).getId(), confirmedOrder.getId()))
        );
    }

    private static RequestFieldsSnippet requestFieldsOrderDto() {
        return requestFields(
                fieldWithPath("userId").description("유저 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].stockId").description("상품 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].quantity").description("상품 주문 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].price").description("상품 주문 금액").type(JsonFieldType.NUMBER)
        );
    }

    private static ResponseFieldsSnippet responseFieldsStockDto() {
        return responseFields(
                fieldWithPath("orderId").description("주문 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("orderNo").description("주문 번호").type(JsonFieldType.STRING),
                fieldWithPath("orderStocks[].stockId").description("상품 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].orderStockId").description("주문 상품 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].quantity").description("상품 주문 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].originalPrice").description("상품 개별 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].totalPrice").description("상품 총 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].orderId").description("주문 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("totalPrice").description("주문 총 금액").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStatus").description("주문 상태").type(JsonFieldType.STRING),
                fieldWithPath("orderDate").description("주문 일시").type(JsonFieldType.STRING)
        );
    }

    private static ResponseFieldsSnippet responseFieldsOrderInfoDto() {
        return responseFields(
                fieldWithPath("orderId").description("주문 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("orderNo").description("주문 번호").type(JsonFieldType.STRING),
                fieldWithPath("totalPrice").description("주문 총 금액").type(JsonFieldType.NUMBER),
                fieldWithPath("orderDate").description("주문 일시").type(JsonFieldType.STRING),
                fieldWithPath("orderStatus").description("주문 상태").type(JsonFieldType.STRING),
                fieldWithPath("stocks[].stockId").description("상품 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].stockName").description("상품명").type(JsonFieldType.STRING),
                fieldWithPath("stocks[].originalPrice").description("상품 개별 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].totalPrice").description("상품 총 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].quantity").description("상품 주문 수량").type(JsonFieldType.NUMBER)
        );
    }

    private static ResponseFieldsSnippet responseFieldsOrderInfoPaginationDto() {
        return responseFields(
                fieldWithPath("orders[].orderId").description("주문 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("orders[].orderNo").description("주문 번호").type(JsonFieldType.STRING),
                fieldWithPath("orders[].totalPrice").description("주문 총 금액").type(JsonFieldType.NUMBER),
                fieldWithPath("orders[].orderDate").description("주문 일시").type(JsonFieldType.STRING),
                fieldWithPath("orders[].orderStatus").description("주문 상태").type(JsonFieldType.STRING),
                fieldWithPath("orders[].stocks[].stockId").description("상품 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("orders[].stocks[].stockName").description("상품명").type(JsonFieldType.STRING),
                fieldWithPath("orders[].stocks[].originalPrice").description("상품 개별 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("orders[].stocks[].totalPrice").description("상품 총 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("orders[].stocks[].quantity").description("상품 주문 수량").type(JsonFieldType.NUMBER),

                fieldWithPath("pageInfo.size").description("조회된 데이터 개수").type(JsonFieldType.NUMBER),
                fieldWithPath("pageInfo.pageNumber").description("현재 페이지 번호").type(JsonFieldType.NUMBER),
                fieldWithPath("pageInfo.last").description("마지막 여부").type(JsonFieldType.BOOLEAN)
        );
    }


}
