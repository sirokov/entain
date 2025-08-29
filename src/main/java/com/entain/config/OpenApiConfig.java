package com.entain.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Sport Events API",
        version = "1.0.0",
        description = "CRUD REST API for managing sport events with status updates and SSE subscriptions",
        contact = @Contact(
            name = "Roman Sirokov",
            email = "sirokov@yahoo.no"
        )
    )
)
public class OpenApiConfig {}
