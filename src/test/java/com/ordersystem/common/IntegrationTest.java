package com.ordersystem.common;

import com.ordersystem.common.config.TestRedisEmbeddedConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({TestRedisEmbeddedConfig.class})
@SpringBootTest
@ActiveProfiles("test")
public @interface IntegrationTest {
}
