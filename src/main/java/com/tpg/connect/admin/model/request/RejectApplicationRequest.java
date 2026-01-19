package com.tpg.connect.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public record RejectApplicationRequest(
        @NotBlank
        String rejectionReason,
        String notes
) {}

