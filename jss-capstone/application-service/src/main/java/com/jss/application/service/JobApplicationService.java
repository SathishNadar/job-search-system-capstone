package com.jss.application.service;

import com.jss.application.dto.ApplicationRequestDTO;
import com.jss.application.dto.ApplicationResponseDTO;
import com.jss.application.dto.StatusUpdateRequestDTO;

import java.util.List;

public interface JobApplicationService {

    ApplicationResponseDTO applyForJob(ApplicationRequestDTO requestDTO);

    ApplicationResponseDTO getApplicationById(Long id);

    List<ApplicationResponseDTO> getApplicationsByJobSeekerId(Long jobSeekerId);

    List<ApplicationResponseDTO> getApplicationsByJobId(Long jobId);

    List<ApplicationResponseDTO> getApplicationsByJobIdAndStatus(Long jobId, String status);

    ApplicationResponseDTO updateApplicationStatus(Long id, StatusUpdateRequestDTO requestDTO);

    void withdrawApplication(Long id);
}
