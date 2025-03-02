package com.ordersystem.integration.order;

import com.ordersystem.order.application.RedisConcurrencyManager;
import com.ordersystem.order.application.RedisSubtractResult;
import com.ordersystem.order.application.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@DisplayName("[Mock] RedisConcurrencyManager 모킹 테스트")
class RedisConcurrencyManagerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private RedisConcurrencyManager redisConcurrencyManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("재고의 차감에 성공할 경우 성공 응답을 반환한다.")
    void subtract_multiple_stocks_success() {
        Map<String, Integer> keyQuantityPair = new HashMap<>();
        keyQuantityPair.put("stock:1", 2);
        keyQuantityPair.put("stock:2", 3);

        List<Object> mockResult = Arrays.asList(1L, Collections.emptyList(), Collections.emptyList());
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class))).thenReturn(mockResult);

        RedisSubtractResult result = redisConcurrencyManager.subtractMultipleStocks(keyQuantityPair);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSoldOutIds()).isEmpty();
        assertThat(result.getNotFoundIds()).isEmpty();
    }

    @Test
    @DisplayName("품절된 재고로 인해 차감에 실패할 경우 실패 응답과 품절된 상품의 Id를 반환한다.")
    void subtract_multiple_stocks_fail_by_sold_out() {
        Map<String, Integer> keyQuantityPair = new HashMap<>();
        keyQuantityPair.put("stock:1", 5);
        keyQuantityPair.put("stock:2", 2);

        List<Object> mockResult = Arrays.asList(-1L, List.of("stock:1"), Collections.emptyList());
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class))).thenReturn(mockResult);

        RedisSubtractResult result = redisConcurrencyManager.subtractMultipleStocks(keyQuantityPair);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getSoldOutIds()).isNotEmpty();
        assertThat(result.getNotFoundIds()).isEmpty();
    }

    @Test
    @DisplayName("키를 찾지 못해 차감에 실패할 경우 실패 응답과 찾지 못한 상품의 Id를 반환한다.")
    void subtract_multiple_stocks_fail_by_not_found() {
        Map<String, Integer> keyQuantityPair = new HashMap<>();
        keyQuantityPair.put("stock:1", 5);
        keyQuantityPair.put("stock:2", 2);

        List<Object> mockResult = Arrays.asList(-1L, List.of(), List.of("stock:1"));
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class))).thenReturn(mockResult);

        RedisSubtractResult result = redisConcurrencyManager.subtractMultipleStocks(keyQuantityPair);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getSoldOutIds()).isEmpty();
        assertThat(result.getNotFoundIds()).isNotEmpty();
    }

    @Test
    @DisplayName("레디스에 재고 정보를 등록하는데 성공할 경우 성공 응답을 반환한다.")
    void set_multiple_stocks_success() {
        Map<String, Integer> keyQuantityPair = new HashMap<>();
        keyQuantityPair.put("stock:1", 10);
        keyQuantityPair.put("stock:2", 15);

        List<Object> mockResult = Arrays.asList(1L, Collections.emptyList());
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class))).thenReturn(mockResult);

        ResultCode result = redisConcurrencyManager.setMultipleStocks(keyQuantityPair);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }
}
