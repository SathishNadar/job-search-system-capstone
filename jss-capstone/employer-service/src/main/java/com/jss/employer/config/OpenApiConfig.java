package com.jss.employer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 (Swagger UI) Configuration for employer-service.
 * Accessible at: http://localhost:8082/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI employerServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Search System — Employer Microservice API")
                        .description("REST API documentation for managing employer organizations in the Job Search System. " +
                                "Runs on Port 8082 and registers dynamically with Eureka Discovery Server (Port 8761).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sathish Nadar")
                                .email("sathishnadar.xiaoguar@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Direct Employer Microservice (Local)"),
                        new Server().url("http://localhost:8080/api/employers").description("API Gateway Route (Future Step 7)")
                ));
    }
}
