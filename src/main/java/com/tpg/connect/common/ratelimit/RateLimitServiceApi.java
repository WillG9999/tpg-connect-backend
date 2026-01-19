package com.tpg.connect.common.ratelimit;

public interface RateLimitServiceApi {
    RateLimitResult tryConsume(String identifier, RateLimitPlan plan);
    boolean isAllowed(String identifier, RateLimitPlan plan);
}

