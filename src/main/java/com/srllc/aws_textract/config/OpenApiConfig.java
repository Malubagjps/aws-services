package com.srllc.aws_textract.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for SWAGGER UI
 */
@Configuration
public class OpenApiConfig {
    // Configure custom OpenAPI documentation

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Security Amazon Services")
                        .version("1.0")
                        .description("""
                                API Documentation for Amazon Textract and Amazon Rekognition
                                
                                **Developers**
                                - Jhon Paul Malubag
                                """)
                        .contact(new Contact()
                                .name("Jhon Paul Malubag")
                                .email("malubagjp.srbootcamp2025@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .name("Authorization")
                                .description("Bearer token")));
    }
}
