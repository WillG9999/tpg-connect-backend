package com.tpg.connect.application.model.request;

import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(@NotNull String email) {
}

