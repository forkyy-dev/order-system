package com.ordersystem.e2e.stock;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.common.ControllerTest;
import com.ordersystem.stock.ui.dto.StockCreateRequest;
import com.ordersystem.stock.ui.dto.StockModifyRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@DisplayName("상품 API 테스트")
public class StockControllerTest extends ControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("관리자는 상품을 등록할 수 있다.")
    void create_new_stock() {
        StockCreateRequest request = StockCreateRequest.builder()
                .stockName("의자")
                .price(10000.0)
                .maxQuantity(20)
                .categoryId(1L)
                .build();

        given(this.spec)
                .filter(
                        document("stock-create",
                                requestFields(
                                        fieldWithPath("stockName").description("상품명").type(JsonFieldType.STRING),
                                        fieldWithPath("price").description("가격").type(JsonFieldType.NUMBER),
                                        fieldWithPath("maxQuantity").description("최대 상품 개수").type(JsonFieldType.NUMBER),
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
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("name", equalTo("의자"));
    }

    @Test
    @DisplayName("관리자는 상품을 수정할 수 있다.")
    void modify_stock() {
        StockModifyRequest request = StockModifyRequest.builder()
                .stockId(1L)
                .stockName("책상")
                .build();

        given(this.spec)
                .filter(
                        document("stock-modify",
                                requestFields(
                                        fieldWithPath("stockId").description("상품 고유 ID").type(JsonFieldType.NUMBER).optional(),
                                        fieldWithPath("stockName").description("상품명").type(JsonFieldType.STRING).optional(),
                                        fieldWithPath("price").description("가격").type(JsonFieldType.NUMBER).optional(),
                                        fieldWithPath("currentQuantity").description("현재 상품 개수").type(JsonFieldType.NUMBER).optional(),
                                        fieldWithPath("maxQuantity").description("최대 상품 개수").type(JsonFieldType.NUMBER).optional(),
                                        fieldWithPath("categoryId").description("카테고리 ID").type(JsonFieldType.NUMBER).optional()
                                ),
                                responseFieldsStockDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .put("/api/stock")
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
                                requestFields(fieldWithPath("stockId").description("상품 고유 ID").type(JsonFieldType.NUMBER))
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
        .when()
                .delete("/api/stock")
        .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("사용자는 카테고리 정보를 바탕으로 상품을 조회할 수 있다.")
    void search_stock() {
        String stockName = "의자";
        Category category = categoryRepository.findById(1L).get();

        given(this.spec)
                .filter(
                        document("stock-search",
                                queryParameters(
                                        parameterWithName("categoryId").description("카테고리 고유 ID"),
                                        parameterWithName("stockName").description("상품명")
                                ),
                                responseFieldsStockPaginationDto()
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .queryParam("categoryId", category.getId())
                .queryParam("stockName", stockName)
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
                fieldWithPath("currentQuantity").description("현재 상품 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("maxQuantity").description("최대 상품 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("categoryId").description("카테고리 고유 ID").type(JsonFieldType.NUMBER)
        );
    }

    private static ResponseFieldsSnippet responseFieldsStockPaginationDto() {
        return responseFields(
                fieldWithPath("stocks[].id").description("상품 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].name").description("상품명").type(JsonFieldType.STRING),
                fieldWithPath("stocks[].price").description("계정 타입").type(JsonFieldType.STRING),
                fieldWithPath("stocks[].currentQuantity").description("현재 상품 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].maxQuantity").description("최대 상품 수량").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].categoryId").description("카테고리 고유 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("stocks[].categoryName").description("카테고리명").type(JsonFieldType.STRING),

                fieldWithPath("page.size").description("조회된 데이터 개수").type(JsonFieldType.NUMBER),
                fieldWithPath("page.nextId").description("다음 검색할 id").type(JsonFieldType.NUMBER),
                fieldWithPath("page.last").description("마지막 여부").type(JsonFieldType.BOOLEAN)
        );
    }
}
