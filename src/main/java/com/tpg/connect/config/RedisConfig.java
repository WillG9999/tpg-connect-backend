package com.tpg.connect.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
@Slf4j
public class RedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    @ConditionalOnProperty(name = "app.redis.embedded", havingValue = "true")
    public void startEmbeddedRedis() throws IOException {
        log.info("Starting embedded Redis server on port 6379");
        redisServer = new RedisServer(6379);
        redisServer.start();
        log.info("Embedded Redis server started successfully");
    }

    @PreDestroy
    public void stopEmbeddedRedis() {
        if (redisServer != null) {
            log.info("Stopping embedded Redis server");
            try {
                redisServer.stop();
            } catch (Exception e) {
                log.warn("Error stopping Redis: " + e.getMessage());
            }
        }
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Creating Redis connection factory");
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Configuring RedisTemplate");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
