package com.jss.employer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI employerServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Search System (JSS) - Employer Service API")
                        .description("Microservice responsible for Employer Registration, Profile Management, and Candidate Search Operations.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("JSS Development Team")
                                .email("support@jobsearchsystem.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
