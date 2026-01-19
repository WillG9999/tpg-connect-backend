package com.tpg.connect.admin.model.response;

import java.util.List;

public record ApplicationsPageResponse(
        List<ApplicationDetailResponse> applications,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {}

