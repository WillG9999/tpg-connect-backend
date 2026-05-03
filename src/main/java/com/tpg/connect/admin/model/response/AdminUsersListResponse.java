package com.tpg.connect.admin.model.response;

import java.util.List;

public record AdminUsersListResponse(
        List<AdminUserSummaryResponse> users,
        int totalElements
) {}
