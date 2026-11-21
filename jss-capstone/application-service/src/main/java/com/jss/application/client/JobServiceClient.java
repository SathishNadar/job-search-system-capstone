package com.jss.application.client;

import com.jss.application.dto.JobSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative REST client for 'job-service' running on Port 8084.
 * Discovered dynamically via Eureka.
 */
@FeignClient(name = "job-service")
public interface JobServiceClient {

    @GetMapping("/api/jobs/{id}")
    JobSummaryDTO getJobById(@PathVariable("id") Long id);
}
