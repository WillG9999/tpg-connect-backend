package com.tpg.connect.admin.model.response;

import java.util.List;

public record PendingApplicationsResponse(
        List<ApplicationDetailResponse> applications,
        int totalCount
) {}

