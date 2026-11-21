package com.jss.application.client;

import com.jss.application.dto.JobSeekerSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative REST client for 'jobseeker-service' running on Port 8083.
 * Discovered dynamically via Eureka.
 */
@FeignClient(name = "jobseeker-service")
public interface JobSeekerServiceClient {

    @GetMapping("/api/jobseekers/{id}")
    JobSeekerSummaryDTO getJobSeekerById(@PathVariable("id") Long id);
}
