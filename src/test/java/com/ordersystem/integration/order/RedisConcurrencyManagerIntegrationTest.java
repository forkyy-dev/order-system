package com.ordersystem.integration.order;

import com.ordersystem.common.IntegrationTest;
import com.ordersystem.common.config.RedisEmbeddedConfig;
import com.ordersystem.order.application.RedisConcurrencyManager;
import com.ordersystem.order.application.RedisSubtractResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[Integration] RedisConcurrencyManager 동시성 통합 테스트")
@Import({RedisEmbeddedConfig.class})
@IntegrationTest
class RedisConcurrencyManagerIntegrationTest {

    @Autowired
    private RedisConcurrencyManager redisConcurrencyManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final int THREAD_COUNT = 10;

    @BeforeEach
    void set_up() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("stock:1", "10");
        ops.set("stock:2", "10");
        ops.set("stock:3", "30");
    }

    @Test
    @DisplayName("동시성 테스트 - Redis에서 재고 감소")
    void subtract_multiple_stocks_concurrency() throws InterruptedException, ExecutionException {
        Map<String, Integer> keyQuantityPair = new HashMap<>();
        keyQuantityPair.put("stock:1", 1);
        keyQuantityPair.put("stock:2", 1);
        keyQuantityPair.put("stock:3", 1);

        int requestCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<RedisSubtractResult>> futures = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {
            futures.add(executorService.submit(() -> redisConcurrencyManager.subtractMultipleStocks(keyQuantityPair)));
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        for (Future<RedisSubtractResult> future : futures) {
            RedisSubtractResult result = future.get();
            assertThat(result.isSuccess()).isTrue();
        }

        // Redis에 남은 재고 확인
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        int remainingStock1 = Integer.parseInt(ops.get("stock:1"));
        int remainingStock2 = Integer.parseInt(ops.get("stock:2"));
        int remainingStock3 = Integer.parseInt(ops.get("stock:3"));

        assertThat(remainingStock1).isEqualTo(0);
        assertThat(remainingStock2).isEqualTo(0);
        assertThat(remainingStock3).isEqualTo(20);
    }

    private int find_available_port() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
