package com.jss.messaging.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 / Swagger UI configuration for Messaging Microservice.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI messagingMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Messaging Microservice API")
                        .description("REST APIs for employer-candidate conversation threads in Job Search System (JSS)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sathish Nadar")
                                .email("sathishmacbook01@gmail.com")));
    }
}
