package com.jss.job.service;

import com.jss.job.dto.JobRequestDTO;
import com.jss.job.dto.JobResponseDTO;

import java.util.List;

public interface JobService {

    JobResponseDTO createJob(JobRequestDTO requestDTO);

    JobResponseDTO getJobById(Long id);

    List<JobResponseDTO> getAllActiveJobs();

    List<JobResponseDTO> searchJobs(String skill, String jobTitle, String designation, String location, Long employerId);

    List<JobResponseDTO> getJobsByEmployerId(Long employerId);

    JobResponseDTO updateJob(Long id, JobRequestDTO requestDTO);

    void deleteJob(Long id);
}
