package com.tpg.connect.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.function.Supplier;

@Slf4j
@Service
public class RateLimitService implements RateLimitServiceApi {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    private ProxyManager<String> proxyManager;

    @PostConstruct
    public void init() {
        if (!rateLimitEnabled) {
            log.info("Rate limiting is disabled");
            return;
        }

        try {
            RedisClient redisClient = RedisClient.create("redis://" + redisHost + ":" + redisPort);
            StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                    RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
            );

            proxyManager = LettuceBasedProxyManager.builderFor(connection)
                    .build();

            log.info("Rate limiting initialized with Redis at {}:{}", redisHost, redisPort);
        } catch (Exception e) {
            log.warn("Redis not available for rate limiting, using in-memory fallback: {}", e.getMessage());
            proxyManager = null;
        }
    }

    public RateLimitResult tryConsume(String identifier, RateLimitPlan plan) {
        if (!rateLimitEnabled || proxyManager == null) {
            return RateLimitResult.allowed(plan.getLimit(), plan.getLimit() - 1);
        }

        String key = buildKey(identifier, plan);

        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(plan.getLimit(), plan.getDuration()))
                .build();

        Bucket bucket = proxyManager.builder().build(key, configSupplier);

        if (bucket.tryConsume(1)) {
            long remaining = bucket.getAvailableTokens();
            return RateLimitResult.allowed(plan.getLimit(), remaining);
        } else {
            long retryAfterSeconds = plan.getDuration().toSeconds();
            return RateLimitResult.blocked(plan.getLimit(), 0, retryAfterSeconds);
        }
    }

    public boolean isAllowed(String identifier, RateLimitPlan plan) {
        return tryConsume(identifier, plan).isAllowed();
    }

    private String buildKey(String identifier, RateLimitPlan plan) {
        return "rate_limit:" + plan.name().toLowerCase() + ":" + identifier;
    }
}

