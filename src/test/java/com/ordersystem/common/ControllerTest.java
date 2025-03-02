package com.ordersystem.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordersystem.common.config.TestRedisEmbeddedConfig;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestRedisEmbeddedConfig.class})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
public class ControllerTest {

    @LocalServerPort
    protected int port;
    @Autowired
    protected StringRedisTemplate redisTemplate;
    @Autowired
    protected ObjectMapper objectMapper;
    protected RequestSpecification spec;
    {
        setUpRestAssured();
    }

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        RestAssured.port = port;
        this.spec = new RequestSpecBuilder()
                .setPort(port)
                .addFilter(
                        documentationConfiguration(restDocumentation)
                                .operationPreprocessors()
                                .withRequestDefaults(prettyPrint())
                                .withResponseDefaults(prettyPrint())
                )
                .build();
    }

    private void setUpRestAssured() {
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(
                        new ObjectMapperConfig(
                                new Jackson2Mapper((type, charset) -> objectMapper)
                        )
                );
    }
}
