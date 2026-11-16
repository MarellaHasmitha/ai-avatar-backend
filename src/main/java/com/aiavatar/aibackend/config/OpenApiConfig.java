
package com.aiavatar.aibackend.config;

import java.util.Collections;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer jwtSecurityCustomizer() {

        return openApi -> {

            // Apply JWT authentication as the default for all operations
            openApi.addSecurityItem(
                new SecurityRequirement()
                    .addList("Bearer Authentication")
            );

            if (openApi.getPaths() == null) {
                return;
            }

            // Exclude public endpoints from JWT authentication
            openApi.getPaths().forEach((path, pathItem) -> {

                pathItem.readOperationsMap().forEach((method, operation) -> {

                    boolean isPublicEndpoint =
                        ("/".equals(path)
                            && method.name().equals("GET"))
                        || ("/api/users".equals(path)
                            && method.name().equals("POST"))
                        || ("/api/auth/login".equals(path)
                            && method.name().equals("POST"))
                        || ("/api/auth/register".equals(path)
                            && method.name().equals("POST"));

                    if (isPublicEndpoint) {
                        operation.setSecurity(Collections.emptyList());
                    }
                });
            });
        };
    }
}
