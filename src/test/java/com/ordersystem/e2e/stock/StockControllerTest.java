package com.ordersystem.e2e.stock;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.ControllerTest;
import com.ordersystem.common.helper.FixtureBuilder;
import com.ordersystem.config.RedisTestConfig;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.ui.dto.StockCreateRequest;
import com.ordersystem.stock.ui.dto.StockModifyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@Import({RedisTestConfig.class})
@DisplayName("[RestDocs] 상품 API 테스트")
class StockControllerTest extends ControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StockRepository stockRepository;

    Category category;
    Stock testStock;

    @BeforeEach
    void setData() {
        stockRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(new Category("카테고리1"));
        testStock = stockRepository.save(new Stock("테스트용 데이터1", 12000D, 20, category.getId()));
        for (int i = 1; i <= 20; i++) {
            stockRepository.save(FixtureBuilder.createSingleStock("테스트 상품" + i, category.getId()));
        }
    }

    @Test
    @DisplayName("관리자는 상품을 등록할 수 있다.")
    void create_new_stock() {
        StockCreateRequest request = StockCreateRequest.builder()
                .stockName("의자")
                .price(10000.0)
                .quantity(20)
                .categoryId(category.getId())
                .build();

        given(this.spec)
                .filter(
                        document("stock-create",
                                requestFields(
                                        fieldWithPath("stockName").description("상품명").type(JsonFieldType.STRING),
                                        fieldWithPath("price").description("가격").type(JsonFieldType.NUMBER),
                                        fieldWithPath("quantity").description("최대 상품 개수").type(JsonFieldType.NUMBER),
                                        fieldWithPath("categoryId").description("카테고리 ID").type(JsonFieldType.NUMBER)
                                ),
                                responseFieldsStockDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .post("/api/stock")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("name", equalTo("의자"));
    }

    @Test
    @DisplayName("관리자는 상품을 수정할 수 있다.")
    void modify_stock() {
        StockModifyRequest request = StockModifyRequest.builder()
                .stockName("책상")
                .price(10000D)
                .quantity(10)
                .categoryId(category.getId())
                .build();

        given(this.spec)
                .filter(
                        document("stock-modify",
                                pathParameters(parameterWithName("stockId").description("상품 ID")),
                                requestFields(
                                        fieldWithPath("stockName").description("상품명").type(JsonFieldType.STRING).optional(),
                                        fieldWithPath("price").description("가격").type(JsonFieldType.NUMBER).optional(),
                                        fieldWithPath("quantity").description("현재 상품 개수").type(JsonFieldType.NUMBER).optional(),
                                        fieldWithPath("categoryId").description("카테고리 ID").type(JsonFieldType.NUMBER).optional()
                                ),
                                responseFieldsStockDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .put("/api/stock/{stockId}", testStock.getId())
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo(request.getStockName()));
    }

    @Test
    @DisplayName("관리자는 상품을 삭제할 수 있다.")
    void delete_new_stock() {
        given(this.spec)
                .filter(
                        document("stock-delete",
                                pathParameters(parameterWithName("stockId").description("상품 ID"))
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
        .when()
                .delete("/api/stock/{stockId}", testStock.getId())
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("사용자는 카테고리 정보와 상품명을 바탕으로 상품 목록을 조회할 수 있다.")
    void search_stock() {
        String stockName = "테스트";
        int pageNumber = 0;

        given(this.spec)
                .filter(
                        document("stock-search",
                                queryParameters(
                                        parameterWithName("categoryId").description("카테고리 고유 ID"),
                                        parameterWithName("stockName").description("상품명"),
                                        parameterWithName("pageNumber").description("페이지 번호")
                                ),
                                responseFieldsStockPaginationDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .queryParam("categoryId", category.getId())
                .queryParam("stockName", stockName)
                .queryParam("pageNumber", pageNumber)
        .when()
                .get("/api/stocks")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    private static ResponseFieldsSnippet responseFieldsStockDto() {
        return responseFields(
                fieldWithPath("id").description("상품 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("name").description("상품명").type(JsonFieldType.STRING),
                fieldWithPath("price").description("계정 타입").type(JsonFieldType.NUMBER),
                fieldWithPath("quantity").description("현재 상품 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("categoryId").description("카테고리 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("categoryName").description("카테고리명").type(JsonFieldType.STRING)
        );
    }

    private static ResponseFieldsSnippet responseFieldsStockPaginationDto() {
        return responseFields(
                fieldWithPath("stocks[].id").description("상품 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].name").description("상품명").type(JsonFieldType.STRING),
                fieldWithPath("stocks[].price").description("계정 타입").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].quantity").description("현재 상품 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].categoryId").description("카테고리 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].categoryName").description("카테고리명").type(JsonFieldType.STRING),

                fieldWithPath("pageInfo.size").description("조회된 데이터 개수").type(JsonFieldType.NUMBER),
                fieldWithPath("pageInfo.pageNumber").description("현재 페이지 번호").type(JsonFieldType.NUMBER),
                fieldWithPath("pageInfo.last").description("마지막 여부").type(JsonFieldType.BOOLEAN)
        );
    }
}
