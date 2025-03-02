package com.ordersystem.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class RedisTestConfig {

    private static final RedisContainer REDIS = new RedisContainer(DockerImageName.parse("redis:7.2"));

    static {
        REDIS.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        int redisPort = REDIS.getMappedPort(6379);
        String redisHost = REDIS.getHost();
        registry.add("spring.data.redis.host", () -> redisHost);
        registry.add("spring.data.redis.port", () -> redisPort);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
    }
}
