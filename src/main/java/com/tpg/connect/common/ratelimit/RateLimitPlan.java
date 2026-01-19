package com.tpg.connect.common.ratelimit;

import java.time.Duration;

public enum RateLimitPlan {

    AUTH(10, Duration.ofMinutes(1)),
    API_STANDARD(100, Duration.ofMinutes(1)),
    MESSAGING(30, Duration.ofMinutes(1)),
    PHOTO_UPLOAD(10, Duration.ofHours(1)),
    REPORTS(5, Duration.ofDays(1));

    private final int limit;
    private final Duration duration;

    RateLimitPlan(int limit, Duration duration) {
        this.limit = limit;
        this.duration = duration;
    }

    public int getLimit() {
        return limit;
    }

    public Duration getDuration() {
        return duration;
    }
}

