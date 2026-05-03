package com.tpg.connect.admin.model.response;

import java.util.Map;

public record AdminUserDetailResponse(
        Map<String, Object> profile,
        String email,
        String applicationStatus,
        boolean active,
        boolean emailVerified,
        Map<String, Object> activityStats
) {}
