package com.jss.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Job Management Microservice.
 * Runs on Port 8084, registers with Eureka Discovery Server (Port 8761),
 * and activates Spring Cloud OpenFeign clients to communicate with employer-service.
 */
@SpringBootApplication
@EnableFeignClients
public class JobServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
}
