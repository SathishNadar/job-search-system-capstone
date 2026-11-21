package com.jss.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 / Swagger UI configuration for Application Microservice.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI applicationMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Application Microservice API")
                        .description("REST APIs for submitting, tracking, and updating job applications in Job Search System (JSS)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sathish Nadar")
                                .email("sathishmacbook01@gmail.com")));
    }
}
