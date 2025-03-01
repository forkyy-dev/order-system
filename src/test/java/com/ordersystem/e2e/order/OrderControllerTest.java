package com.ordersystem.e2e.order;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.ControllerTest;
import com.ordersystem.common.helper.FixtureBuilder;
import com.ordersystem.order.ui.dto.OrderCreateRequest;
import com.ordersystem.order.ui.dto.OrderStockRequest;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@DisplayName("[RestDocs] 주문 API 테스트")
class OrderControllerTest extends ControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StockRepository stockRepository;

    Category category;
    List<Stock> stocks;

    @BeforeEach
    void setData() {
        stockRepository.deleteAll();
        categoryRepository.deleteAll();

        stocks = new ArrayList<>();
        category = categoryRepository.save(new Category("카테고리1"));
        for (int i = 1; i <= 20; i++) {
            stocks.add(stockRepository.save(FixtureBuilder.createSingleStock("상품" + i, category.getId())));
        }
    }

    @Test
    @DisplayName("사용자는 주문을 생성할 수 있다.")
    void create_order_success() {
        Stock stock = stocks.get(0);
        int quantity = 2;
        double price = stock.getPrice() * quantity;
        OrderStockRequest stockRequest = new OrderStockRequest(stock.getId(), quantity, price);
        OrderCreateRequest request = new OrderCreateRequest(1L, List.of(stockRequest));

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
                .log().all()
                .statusCode(HttpStatus.CREATED.value());
    }


//    @Test
//    @DisplayName("사용자는 자신의 주문 목록을 조회할 수 있다.")
//    void search_order_success() {
//
//    }
//
//    @Test
//    @DisplayName("사용자는 주문에 대한 상세 정보 조회를 할 수 있다.")
//    void search_order_detail_success() {
//
//    }


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
                fieldWithPath("orderNumber").description("주문 번호").type(JsonFieldType.STRING),
                fieldWithPath("orderStocks[].stockId").description("계정 타입").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].quantity").description("상품 주문 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("orderStocks[].price").description("상품 주문 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("totalPrice").description("주문 총 금액").type(JsonFieldType.NUMBER),
                fieldWithPath("status").description("주문 상태").type(JsonFieldType.STRING),
                fieldWithPath("orderDate").description("주문 일시").type(JsonFieldType.STRING)
        );
    }

}
