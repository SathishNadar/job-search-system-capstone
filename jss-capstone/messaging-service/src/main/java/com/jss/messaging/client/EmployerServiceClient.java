package com.jss.messaging.client;

import com.jss.messaging.dto.EmployerSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "employer-service")
public interface EmployerServiceClient {

    @GetMapping("/api/employers/{id}")
    EmployerSummaryDTO getEmployerById(@PathVariable("id") Long id);
}
