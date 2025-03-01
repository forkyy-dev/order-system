package com.ordersystem.common.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.util.StringUtils;
import redis.embedded.RedisServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@TestConfiguration
public class RedisEmbeddedConfig {
    private static final int REDIS_PORT = 6379;
    private RedisServer redisServer;

    @PostConstruct
    public void configRedisServer() throws IOException {
        int port = REDIS_PORT;
        if (isProcessRunning(getProcess(port))) {
            port = getAvailablePort();
        }

        redisServer = new RedisServer(port);
        redisServer.start();
    }

    @PreDestroy
    public void stopRedisServer() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    private int getAvailablePort() throws IOException {
        for (int port = 10000; port <= 65535; port++) {
            Process process = getProcess(port);
            if (!isProcessRunning(process)) {
                return port;
            }
        }

        throw new RuntimeException("available port is not exist between 10000 and 65535.");
    }

    private Process getProcess(int port) throws IOException {
        String command = String.format("netstat -nat | grep LISTEN | grep %d", port);
        String[] shell = {"/bin/sh", "-c", command};
        return Runtime.getRuntime().exec(shell);
    }


    private boolean isProcessRunning(Process process) {
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("process running read fail.");
        }

        return StringUtils.hasText(stringBuilder.toString());
    }
}
