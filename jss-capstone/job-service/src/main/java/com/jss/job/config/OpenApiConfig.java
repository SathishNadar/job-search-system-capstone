package com.jss.job.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 (Swagger UI) Configuration for job-service.
 * Accessible at: http://localhost:8084/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jobServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Search System — Job Management Microservice API")
                        .description("REST API documentation for publishing, managing, and searching job openings across employers. " +
                                "Runs on Port 8084, registers with Eureka (Port 8761), and communicates with employer-service via OpenFeign.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SathishNadar")
                                .email("sathishmacbook01@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Direct Job Microservice (Local)"),
                        new Server().url("http://localhost:8080/api/jobs").description("API Gateway Route (Future Step 7)")
                ));
    }
}
