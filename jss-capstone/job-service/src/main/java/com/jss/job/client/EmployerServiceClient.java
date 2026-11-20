package com.jss.job.client;

import com.jss.job.dto.EmployerSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative OpenFeign client for communicating synchronously with 'employer-service'.
 * Resolves the service network location dynamically through Eureka Discovery
 */
@FeignClient(name = "employer-service")
public interface EmployerServiceClient {

    @GetMapping("/api/employers/{id}")
    EmployerSummaryDTO getEmployerById(@PathVariable("id") Long id);
}
