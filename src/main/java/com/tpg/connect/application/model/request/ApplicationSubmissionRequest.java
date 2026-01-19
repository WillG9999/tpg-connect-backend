package com.tpg.connect.application.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ApplicationSubmissionRequest(
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull String dateOfBirth,
        @NotNull String email,
        @NotNull String password,
        @NotNull String gender,
        @NotNull String location,
        @NotEmpty List<String> bestQualities,
        @NotNull String reasonForJoining,
        List<MultipartFile> photos
) {
}

