package com.jss.messaging.client;

import com.jss.messaging.dto.JobSeekerSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "jobseeker-service")
public interface JobSeekerServiceClient {

    @GetMapping("/api/jobseekers/{id}")
    JobSeekerSummaryDTO getJobSeekerById(@PathVariable("id") Long id);
}
