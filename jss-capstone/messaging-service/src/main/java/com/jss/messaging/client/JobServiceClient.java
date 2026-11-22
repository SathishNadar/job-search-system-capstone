package com.jss.messaging.client;

import com.jss.messaging.dto.JobSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service")
public interface JobServiceClient {

    @GetMapping("/api/jobs/{id}")
    JobSummaryDTO getJobById(@PathVariable("id") Long id);
}
