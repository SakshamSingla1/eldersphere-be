package com.eldersphere.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ElderSphere Backend APIs",
                version = "v1",
                description = "REST API for the ElderSphere elderly-care marketplace platform — covers auth, caretaker discovery, bookings, medical records, reviews, emergency alerts, notifications, landing page content, and admin management."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Paste your JWT access token here (without the 'Bearer ' prefix). Obtain it from POST /api/v1/auth/login."
)
public class SwaggerConfig {
}
