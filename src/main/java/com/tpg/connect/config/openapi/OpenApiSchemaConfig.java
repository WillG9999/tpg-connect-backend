package com.tpg.connect.config.openapi;

import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiSchemaConfig {

    @Bean
    public OpenApiCustomizer schemaCustomizer() {
        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            if (schemas == null) return;

            schemas.forEach((name, schema) -> {
                switch (name) {
                    case "UserRegistrationRequest" -> configureUserRegistrationRequest(schema);
                    case "UserRegistrationResponse" -> configureUserRegistrationResponse(schema);
                    case "ApproveApplicationRequest" -> configureApproveApplicationRequest(schema);
                    case "RejectApplicationRequest" -> configureRejectApplicationRequest(schema);
                    case "ApplicationDetailResponse" -> configureApplicationDetailResponse(schema);
                    case "ApplicationsPageResponse" -> configureApplicationsPageResponse(schema);
                    case "ApplicationSubmissionRequest" -> configureApplicationSubmissionRequest(schema);
                    case "ApplicationStatusResponse" -> configureApplicationStatusResponse(schema);
                    case "LoginRequest" -> configureLoginRequest(schema);
                    case "LoginResponse" -> configureLoginResponse(schema);
                    case "SendVerificationCodeRequest" -> configureSendVerificationCodeRequest(schema);
                    case "VerifyEmailCodeRequest" -> configureVerifyEmailCodeRequest(schema);
                    case "ForgotPasswordRequest" -> configureForgotPasswordRequest(schema);
                    case "ResetPasswordRequest" -> configureResetPasswordRequest(schema);
                    case "VerifyResetTokenRequest" -> configureVerifyResetTokenRequest(schema);
                    case "ChangePasswordRequest" -> configureChangePasswordRequest(schema);
                    case "PasswordResetResponse" -> configurePasswordResetResponse(schema);
                    case "VerifyResetTokenResponse" -> configureVerifyResetTokenResponse(schema);
                }
            });
        };
    }

    private void configureUserRegistrationRequest(Schema schema) {
        schema.setDescription("User registration request payload");
        Map<String, Schema> props = schema.getProperties();
        props.get("email").description("User's email address").example("john.doe@example.com");
        props.get("password").description("User's password (min 8 chars)").example("SecureP@ss123");
        props.get("firstName").description("User's first name").example("John");
        props.get("dateOfBirth").description("Date of birth (YYYY-MM-DD)").example("1990-05-15");
        props.get("gender").description("User's gender").example("Male");
        props.get("location").description("User's location").example("London, UK");
    }

    private void configureUserRegistrationResponse(Schema schema) {
        schema.setDescription("User registration response with JWT bearer");
        Map<String, Schema> props = schema.getProperties();
        props.get("bearer").description("JWT authentication bearer").example("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
    }

    private void configureApproveApplicationRequest(Schema schema) {
        schema.setDescription("Request to approve an application");
        Map<String, Schema> props = schema.getProperties();
        props.get("notes").description("Admin notes for approval").example("Great profile, approved");
    }

    private void configureRejectApplicationRequest(Schema schema) {
        schema.setDescription("Request to reject an application");
        Map<String, Schema> props = schema.getProperties();
        props.get("rejectionReason").description("Reason for rejection").example("incomplete_profile");
        props.get("notes").description("Additional notes").example("Please provide clearer photos");
    }

    private void configureApplicationDetailResponse(Schema schema) {
        schema.setDescription("Detailed application information");
        Map<String, Schema> props = schema.getProperties();
        props.get("applicationId").description("Application ID").example("APP-1234567890");
        props.get("email").description("Applicant email").example("user@example.com");
        props.get("firstName").description("First name").example("John");
        props.get("lastName").description("Last name").example("Doe");
        props.get("dateOfBirth").description("Date of birth").example("1990-01-15");
        props.get("gender").description("Gender").example("Male");
        props.get("location").description("Location").example("San Francisco, CA");
        props.get("bestQualities").description("Best qualities selected by applicant");
        props.get("reasonForJoining").description("Reason for joining").example("Looking for meaningful connections");
        props.get("photoUrls").description("Photo URLs");
        props.get("status").description("Application status").example("pending");
        props.get("submittedAt").description("Submission timestamp").example("2026-01-15T10:30:00Z");
        props.get("reviewedAt").description("Review timestamp").example("2026-01-16T14:00:00Z");
        props.get("reviewNotes").description("Admin review notes");
        props.get("rejectionReason").description("Rejection reason if rejected");
    }

    private void configureApplicationsPageResponse(Schema schema) {
        schema.setDescription("Paginated list of applications");
        Map<String, Schema> props = schema.getProperties();
        props.get("applications").description("List of applications on current page");
        props.get("page").description("Current page number (0-based)").example(0);
        props.get("size").description("Number of items per page").example(20);
        props.get("totalElements").description("Total number of applications").example(150);
        props.get("totalPages").description("Total number of pages").example(8);
        props.get("first").description("Is this the first page").example(true);
        props.get("last").description("Is this the last page").example(false);
    }

    private void configureApplicationSubmissionRequest(Schema schema) {
        schema.setDescription("Application submission request");
        Map<String, Schema> props = schema.getProperties();
        props.get("email").description("Applicant email").example("applicant@example.com");
        props.get("password").description("Password (base64 encoded)").example("U2VjdXJlUEBzczEyMw==");
        props.get("firstName").description("First name").example("John");
        props.get("lastName").description("Last name").example("Doe");
        props.get("dateOfBirth").description("Date of birth").example("1990-01-15");
        props.get("gender").description("Gender").example("Male");
        props.get("location").description("Location").example("San Francisco, CA");
        props.get("bestQualities").description("List of best qualities");
        props.get("reasonForJoining").description("Reason for joining").example("Looking for meaningful connections");
    }

    private void configureApplicationStatusResponse(Schema schema) {
        schema.setDescription("Application status response");
        Map<String, Schema> props = schema.getProperties();
        props.get("applicationId").description("Application ID").example("APP-1234567890");
        props.get("status").description("Application status").example("pending");
        props.get("submittedAt").description("Submission timestamp").example("2026-01-15T10:30:00Z");
    }

    private void configureLoginRequest(Schema schema) {
        schema.setDescription("Login request");
        Map<String, Schema> props = schema.getProperties();
        props.get("email").description("User email").example("user@example.com");
        props.get("password").description("Password (base64 encoded)").example("U2VjdXJlUEBzczEyMw==");
    }

    private void configureLoginResponse(Schema schema) {
        schema.setDescription("Login response");
        Map<String, Schema> props = schema.getProperties();
        props.get("token").description("JWT authentication token").example("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        props.get("isAdmin").description("Whether user is admin").example(false);
    }

    private void configureSendVerificationCodeRequest(Schema schema) {
        schema.setDescription("Send verification code request");
        Map<String, Schema> props = schema.getProperties();
        props.get("email").description("Email to send code to").example("user@example.com");
        props.get("userName").description("User's name").example("John Doe");
    }

    private void configureVerifyEmailCodeRequest(Schema schema) {
        schema.setDescription("Verify email code request");
        Map<String, Schema> props = schema.getProperties();
        props.get("email").description("Email address").example("user@example.com");
        props.get("code").description("Verification code").example("123456");
    }

    private void configureForgotPasswordRequest(Schema schema) {
        schema.setDescription("Forgot password request");
        Map<String, Schema> props = schema.getProperties();
        props.get("email").description("Email address").example("user@example.com");
    }

    private void configureResetPasswordRequest(Schema schema) {
        schema.setDescription("Reset password request");
        Map<String, Schema> props = schema.getProperties();
        props.get("token").description("Reset token").example("abc123def456");
        props.get("newPassword").description("New password (base64 encoded)").example("U2VjdXJlUEBzczEyMw==");
    }

    private void configureVerifyResetTokenRequest(Schema schema) {
        schema.setDescription("Verify reset token request");
        Map<String, Schema> props = schema.getProperties();
        props.get("token").description("Reset token to verify").example("abc123def456");
    }

    private void configureChangePasswordRequest(Schema schema) {
        schema.setDescription("Change password request");
        Map<String, Schema> props = schema.getProperties();
        props.get("currentPassword").description("Current password (base64 encoded)").example("T2xkUEBzczEyMw==");
        props.get("newPassword").description("New password (base64 encoded)").example("TmV3UEBzczEyMw==");
    }

    private void configurePasswordResetResponse(Schema schema) {
        schema.setDescription("Password reset response");
        Map<String, Schema> props = schema.getProperties();
        props.get("success").description("Whether operation was successful").example(true);
        props.get("message").description("Response message").example("Password reset successful");
    }

    private void configureVerifyResetTokenResponse(Schema schema) {
        schema.setDescription("Verify reset token response");
        Map<String, Schema> props = schema.getProperties();
        props.get("valid").description("Whether token is valid").example(true);
        props.get("email").description("Email associated with token").example("user@example.com");
    }
}
