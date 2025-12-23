package com.tpg.connect.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("The Prometheus Group Connect Backend")
                        .description("Backend End System for Connect ")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("TPG Team")
                                .email("support@tpg.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:9000")
                                .description("Development server"),
                        new Server()
                                .url("http://localhost:10000")
                                .description("Test server")
                ));
    }
}