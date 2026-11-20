package com.jss.job.service.impl;

import com.jss.job.client.EmployerServiceClient;
import com.jss.job.dto.EmployerSummaryDTO;
import com.jss.job.dto.JobRequestDTO;
import com.jss.job.dto.JobResponseDTO;
import com.jss.job.entity.Job;
import com.jss.job.exception.DuplicateJobException;
import com.jss.job.exception.EmployerNotFoundException;
import com.jss.job.exception.JobNotFoundException;
import com.jss.job.repository.JobRepository;
import com.jss.job.service.JobService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of JobService business logic.
 * Communicates with 'employer-service' via OpenFeign to validate hiring organizations
 * and manages job lifecycle in MySQL ('job_db').
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final EmployerServiceClient employerServiceClient;

    @Override
    @Transactional
    public JobResponseDTO createJob(JobRequestDTO requestDTO) {
        // 1. Verify Employer exists via OpenFeign call to employer-service (Port 8082)
        EmployerSummaryDTO employer = verifyEmployerExists(requestDTO.getEmployerId());

        // 2. Prevent duplicate active jobs with the same title for this employer
        String trimmedTitle = requestDTO.getJobTitle().trim();
        if (jobRepository.existsByEmployerIdAndJobTitleIgnoreCaseAndStatus(
                requestDTO.getEmployerId(), trimmedTitle, "ACTIVE")) {
            throw new DuplicateJobException(requestDTO.getEmployerId(), trimmedTitle);
        }

        // 3. Persist Job to MySQL
        Job job = mapToEntity(requestDTO);
        job.setJobTitle(trimmedTitle);
        Job savedJob = jobRepository.save(job);

        return mapToResponseDTO(savedJob, employer);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponseDTO getJobById(Long id) {
        Job job = jobRepository.findById(id)
                               .orElseThrow(() -> new JobNotFoundException(id));

        EmployerSummaryDTO employer = fetchEmployerSafe(job.getEmployerId());
        return mapToResponseDTO(job, employer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDTO> getAllActiveJobs() {
        return jobRepository.findByStatusIgnoreCase("ACTIVE").stream()
                .map(job -> mapToResponseDTO(job, fetchEmployerSafe(job.getEmployerId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDTO> searchJobs(String skill, String jobTitle, String designation, String location, Long employerId) {
        String cleanSkill = (skill != null && !skill.trim().isEmpty()) ? skill.trim() : null;
        String cleanTitle = (jobTitle != null && !jobTitle.trim().isEmpty()) ? jobTitle.trim() : null;
        String cleanDesignation = (designation != null && !designation.trim().isEmpty()) ? designation.trim() : null;
        String cleanLocation = (location != null && !location.trim().isEmpty()) ? location.trim() : null;

        return jobRepository.searchJobs(cleanSkill, cleanTitle, cleanDesignation, cleanLocation, employerId).stream()
                .map(job -> mapToResponseDTO(job, fetchEmployerSafe(job.getEmployerId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDTO> getJobsByEmployerId(Long employerId) {
        EmployerSummaryDTO employer = fetchEmployerSafe(employerId);
        return jobRepository.findByEmployerId(employerId).stream()
                .map(job -> mapToResponseDTO(job, employer))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobResponseDTO updateJob(Long id, JobRequestDTO requestDTO) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        // If employer is being changed, verify the new employer exists
        if (!existingJob.getEmployerId().equals(requestDTO.getEmployerId())) {
            verifyEmployerExists(requestDTO.getEmployerId());
            existingJob.setEmployerId(requestDTO.getEmployerId());
        }

        existingJob.setJobTitle(requestDTO.getJobTitle().trim());
        existingJob.setDesignation(requestDTO.getDesignation());
        existingJob.setDescription(requestDTO.getDescription());
        existingJob.setRequiredSkills(requestDTO.getRequiredSkills());
        existingJob.setExperienceRequired(requestDTO.getExperienceRequired());
        existingJob.setQualification(requestDTO.getQualification());
        existingJob.setSalaryMin(requestDTO.getSalaryMin());
        existingJob.setSalaryMax(requestDTO.getSalaryMax());
        existingJob.setLocation(requestDTO.getLocation());
        existingJob.setNoticePeriod(requestDTO.getNoticePeriod());
        existingJob.setContactEmail(requestDTO.getContactEmail());
        existingJob.setEmploymentType(requestDTO.getEmploymentType());

        if (requestDTO.getStatus() != null && !requestDTO.getStatus().trim().isEmpty()) {
            existingJob.setStatus(requestDTO.getStatus().trim().toUpperCase());
        }

        Job updatedJob = jobRepository.save(existingJob);
        EmployerSummaryDTO employer = fetchEmployerSafe(updatedJob.getEmployerId());
        return mapToResponseDTO(updatedJob, employer);
    }

    @Override
    @Transactional
    public void deleteJob(Long id) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        // Soft-delete / deactivate job so existing candidate application history is preserved
        existingJob.setStatus("INACTIVE");
        jobRepository.save(existingJob);
    }

    // Helper: Enforces that employer exists via OpenFeign (throws 404 if missing)
    private EmployerSummaryDTO verifyEmployerExists(Long employerId) {
        try {
            EmployerSummaryDTO employer = employerServiceClient.getEmployerById(employerId);
            if (employer == null) {
                throw new EmployerNotFoundException(employerId);
            }
            return employer;
        } catch (FeignException.NotFound ex) {
            throw new EmployerNotFoundException(employerId);
        }
    }

    // Helper: Safely retrieves employer details without breaking job queries if employer-service is down
    private EmployerSummaryDTO fetchEmployerSafe(Long employerId) {
        try {
            return employerServiceClient.getEmployerById(employerId);
        } catch (Exception ex) {
            log.warn("Could not retrieve company profile for employer ID {}: {}", employerId, ex.getMessage());
            return EmployerSummaryDTO.builder()
                    .employerId(employerId)
                    .organizationName("Company #" + employerId)
                    .build();
        }
    }

    // Mapper helpers
    private Job mapToEntity(JobRequestDTO dto) {
        return Job.builder()
                .employerId(dto.getEmployerId())
                .jobTitle(dto.getJobTitle())
                .designation(dto.getDesignation())
                .description(dto.getDescription())
                .requiredSkills(dto.getRequiredSkills())
                .experienceRequired(dto.getExperienceRequired())
                .qualification(dto.getQualification())
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .location(dto.getLocation())
                .noticePeriod(dto.getNoticePeriod())
                .contactEmail(dto.getContactEmail())
                .employmentType(dto.getEmploymentType())
                .status((dto.getStatus() != null && !dto.getStatus().isBlank()) ? dto.getStatus().trim().toUpperCase() : "ACTIVE")
                .build();
    }

    private JobResponseDTO mapToResponseDTO(Job entity, EmployerSummaryDTO employer) {
        return JobResponseDTO.builder()
                .jobId(entity.getJobId())
                .employerId(entity.getEmployerId())
                .employer(employer)
                .jobTitle(entity.getJobTitle())
                .designation(entity.getDesignation())
                .description(entity.getDescription())
                .requiredSkills(entity.getRequiredSkills())
                .experienceRequired(entity.getExperienceRequired())
                .qualification(entity.getQualification())
                .salaryMin(entity.getSalaryMin())
                .salaryMax(entity.getSalaryMax())
                .location(entity.getLocation())
                .noticePeriod(entity.getNoticePeriod())
                .contactEmail(entity.getContactEmail())
                .employmentType(entity.getEmploymentType())
                .status(entity.getStatus())
                .createdDate(entity.getCreatedDate())
                .build();
    }
}
