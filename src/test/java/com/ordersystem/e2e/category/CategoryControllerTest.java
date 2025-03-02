package com.ordersystem.e2e.category;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.category.ui.dto.CategoryCreateRequest;
import com.ordersystem.category.ui.dto.CategoryModifyRequest;
import com.ordersystem.common.ControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@DisplayName("[RestDocs] 카테고리 API 테스트")
class CategoryControllerTest extends ControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;

    Category category;

    @BeforeEach
    void setData() {
        categoryRepository.deleteAll();

        category = categoryRepository.save(new Category("테스트용 데이터1"));
    }

    @Test
    @DisplayName("관리자는 카테고리를 등록할 수 있다.")
    void create_new_category_success() {
        CategoryCreateRequest request = new CategoryCreateRequest("카테고리1");

        given(this.spec)
                .filter(
                        document("category-create",
                                requestFields(
                                        fieldWithPath("categoryName").description("카테고리명").type(JsonFieldType.STRING)
                                ),
                                responseFields(
                                        fieldWithPath("categoryId").description("카테고리 고유 ID").type(JsonFieldType.NUMBER),
                                        fieldWithPath("categoryName").description("카테고리명").type(JsonFieldType.STRING)
                                )
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .post("/api/category")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("categoryName", equalTo("카테고리1"));
    }

    @Test
    @DisplayName("관리자는 카테고리를 수정할 수 있다.")
    void modify_category_success() {
        CategoryModifyRequest request = new CategoryModifyRequest("변경된 카테고리");

        given(this.spec)
                .filter(
                        document("category-modify",
                                pathParameters(parameterWithName("categoryId").description("카테고리 ID")),
                                requestFields(
                                        fieldWithPath("categoryName").description("상품명").type(JsonFieldType.STRING)
                                ),
                                responseFields(
                                        fieldWithPath("categoryId").description("상품 고유 ID").type(JsonFieldType.NUMBER),
                                        fieldWithPath("categoryName").description("상품명").type(JsonFieldType.STRING)
                                )
                        )
                ).accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .put("/api/category/{categoryId}", category.getId())
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("categoryName", equalTo(request.getCategoryName()));
    }

    @Test
    @DisplayName("관리자는 카테고리를 삭제할 수 있다.")
    void delete_category_success() {
        given(this.spec)
                .filter(document("category-delete", pathParameters(parameterWithName("categoryId").description("카테고리 ID"))))
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
        .when()
                .delete("/api/category/{categoryId}", category.getId())
        .then()
                .statusCode(HttpStatus.OK.value());
    }


}
