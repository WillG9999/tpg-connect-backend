package com.tpg.connect.admin.model.response;

import java.util.Map;

public record DemographicsStatsResponse(
        long totalUsers,
        Map<String, Long> genderDistribution,
        Map<String, Long> interestDistribution
) {}
