package com.tpg.connect.unit.ratelimit;

import com.tpg.connect.common.ratelimit.RateLimitPlan;
import com.tpg.connect.common.ratelimit.RateLimitResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitPlanTest {

    @Test
    void auth_hasCorrectLimits() {
        assertEquals(10, RateLimitPlan.AUTH.getLimit());
        assertEquals(Duration.ofMinutes(1), RateLimitPlan.AUTH.getDuration());
    }

    @Test
    void apiStandard_hasCorrectLimits() {
        assertEquals(100, RateLimitPlan.API_STANDARD.getLimit());
        assertEquals(Duration.ofMinutes(1), RateLimitPlan.API_STANDARD.getDuration());
    }

    @Test
    void messaging_hasCorrectLimits() {
        assertEquals(30, RateLimitPlan.MESSAGING.getLimit());
        assertEquals(Duration.ofMinutes(1), RateLimitPlan.MESSAGING.getDuration());
    }

    @Test
    void photoUpload_hasCorrectLimits() {
        assertEquals(10, RateLimitPlan.PHOTO_UPLOAD.getLimit());
        assertEquals(Duration.ofHours(1), RateLimitPlan.PHOTO_UPLOAD.getDuration());
    }

    @Test
    void reports_hasCorrectLimits() {
        assertEquals(5, RateLimitPlan.REPORTS.getLimit());
        assertEquals(Duration.ofDays(1), RateLimitPlan.REPORTS.getDuration());
    }
}

