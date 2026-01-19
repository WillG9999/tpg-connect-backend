package com.tpg.connect.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.embedded.RedisServer;

@Configuration
@Slf4j
public class RedisConfig {

    private RedisServer redisServer;

    @Value("${app.redis.embedded:false}")
    private boolean embeddedRedisEnabled;

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @PostConstruct
    public void startEmbeddedRedis() {
        if (!embeddedRedisEnabled) {
            log.info("Embedded Redis disabled");
            return;
        }
        try {
            log.info("Starting embedded Redis server on port {}", redisPort);
            redisServer = new RedisServer(redisPort);
            redisServer.start();
            log.info("Embedded Redis server started successfully on port {}", redisPort);
        } catch (Exception e) {
            log.warn("Failed to start embedded Redis on port {}: {}", redisPort, e.getMessage());
        }
    }

    @PreDestroy
    public void stopEmbeddedRedis() {
        if (redisServer != null) {
            log.info("Stopping embedded Redis server");
            try {
                redisServer.stop();
            } catch (Exception e) {
                log.warn("Error stopping Redis: {}", e.getMessage());
            }
        }
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Creating Redis connection factory for {}:{}", redisHost, redisPort);
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Configuring RedisTemplate");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
