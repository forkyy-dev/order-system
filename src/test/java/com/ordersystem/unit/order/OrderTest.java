package com.ordersystem.unit.order;

import com.ordersystem.order.domain.Order;
import com.ordersystem.order.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[Unit] 주문 Entity 테스트")
class OrderTest {

    @Test
    @DisplayName("최초 주문 생성 시 준비 상태로 생성된다.")
    void wait_status() {
        Order order = new Order(1000D, 1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PRE);
    }

    @Test
    @DisplayName("주문의 상태는 변경이 가능하다.")
    void update_status() {
        //given
        Order order = new Order(1000D, 1L);

        //when
        order.confirm();

        //then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRM);
    }
}
