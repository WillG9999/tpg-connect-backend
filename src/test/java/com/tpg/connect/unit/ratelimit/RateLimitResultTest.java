package com.tpg.connect.unit.ratelimit;

import com.tpg.connect.common.ratelimit.RateLimitResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitResultTest {

    @Test
    void allowed_createsAllowedResult() {
        RateLimitResult result = RateLimitResult.allowed(100, 99);

        assertTrue(result.isAllowed());
        assertEquals(100, result.getLimit());
        assertEquals(99, result.getRemaining());
        assertEquals(0, result.getRetryAfterSeconds());
    }

    @Test
    void blocked_createsBlockedResult() {
        RateLimitResult result = RateLimitResult.blocked(100, 0, 60);

        assertFalse(result.isAllowed());
        assertEquals(100, result.getLimit());
        assertEquals(0, result.getRemaining());
        assertEquals(60, result.getRetryAfterSeconds());
    }
}

