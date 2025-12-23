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
        schema.setDescription("User registration response with JWT token");
        Map<String, Schema> props = schema.getProperties();

        props.get("token").description("JWT authentication token").example("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
    }
}
