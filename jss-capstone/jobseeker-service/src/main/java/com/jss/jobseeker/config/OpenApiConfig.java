package com.jss.jobseeker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI jobSeekerServiceOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Job Search System — Job Seeker Microservice API")
                                                .description("REST API documentation for candidate registration, profile management, and unified candidate search. "
                                                                +
                                                                "Runs on Port 8083 and registers dynamically with Eureka Discovery Server (Port 8761).")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Sathish Nadar")
                                                                .email("sathish.mac23@gmail.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://springdoc.org")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8083")
                                                                .description("Direct Job Seeker Microservice (Local)"),
                                                new Server().url("http://localhost:8080/api/jobseekers")
                                                                .description("API Gateway Route (Future Step 7)")));
        }
}
