package com.ordersystem.integration.order;

import com.ordersystem.common.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[Integration] 주문 조회 통합 테스트")
@IntegrationTest
class OrderReadServiceTest {

    @Test
    @DisplayName("사용자는 자신의 주문 목록을 조회할 수 있다.")
    void search_order_success() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("사용자는 주문에 대한 상세 정보 조회를 할 수 있다.")
    void search_order_detail_success() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("상품의 정보가 변경되어도 주문 시점의 정보를 그대로 제공할 수 있다.")
    void get_original_order_data() {
        //given

        //when

        //then
    }

}
