package com.jss.application.service.impl;

import com.jss.application.client.JobServiceClient;
import com.jss.application.client.JobSeekerServiceClient;
import com.jss.application.dto.*;
import com.jss.application.entity.JobApplication;
import com.jss.application.exception.*;
import com.jss.application.repository.JobApplicationRepository;
import com.jss.application.service.JobApplicationService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of JobApplicationService business operations.
 * Coordinates candidate validation via 'jobseeker-service' (Port 8083),
 * job validation via 'job-service' (Port 8084), and manages recruitment
 * lifecycle state in MySQL.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobSeekerServiceClient jobSeekerServiceClient;
    private final JobServiceClient jobServiceClient;

    @Override
    @Transactional
    public ApplicationResponseDTO applyForJob(ApplicationRequestDTO requestDTO) {
        Long seekerId = requestDTO.getJobSeekerId();
        Long jobId = requestDTO.getJobId();

        // 1. Verify candidate exists via OpenFeign
        JobSeekerSummaryDTO candidate = verifyCandidateExists(seekerId);

        // 2. Verify job opening exists & is ACTIVE via OpenFeign
        JobSummaryDTO job = verifyJobExistsAndActive(jobId);

        // 3. Prevent duplicate applications
        if (applicationRepository.existsByJobIdAndJobSeekerId(jobId, seekerId)) {
            throw new DuplicateApplicationException(jobId, seekerId);
        }

        // 4. Persist Application to MySQL
        JobApplication application = JobApplication.builder()
                .jobId(jobId)
                .jobSeekerId(seekerId)
                .resumeUrl(requestDTO.getResumeUrl())
                .applicationStatus("APPLIED")
                .applicationDate(LocalDateTime.now())
                .build();

        JobApplication saved = applicationRepository.save(application);
        return mapToResponseDTO(saved, candidate, job);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDTO getApplicationById(Long id) {
        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        JobSeekerSummaryDTO candidate = fetchCandidateSafe(application.getJobSeekerId());
        JobSummaryDTO job = fetchJobSafe(application.getJobId());

        return mapToResponseDTO(application, candidate, job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getApplicationsByJobSeekerId(Long jobSeekerId) {
        JobSeekerSummaryDTO candidate = fetchCandidateSafe(jobSeekerId);

        return applicationRepository.findByJobSeekerId(jobSeekerId).stream()
                .map(app -> mapToResponseDTO(app, candidate, fetchJobSafe(app.getJobId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getApplicationsByJobId(Long jobId) {
        JobSummaryDTO job = fetchJobSafe(jobId);

        return applicationRepository.findByJobId(jobId).stream()
                .map(app -> mapToResponseDTO(app, fetchCandidateSafe(app.getJobSeekerId()), job))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getApplicationsByJobIdAndStatus(Long jobId, String status) {
        JobSummaryDTO job = fetchJobSafe(jobId);

        return applicationRepository.findByJobIdAndApplicationStatusIgnoreCase(jobId, status.trim()).stream()
                .map(app -> mapToResponseDTO(app, fetchCandidateSafe(app.getJobSeekerId()), job))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApplicationResponseDTO updateApplicationStatus(Long id, StatusUpdateRequestDTO requestDTO) {
        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        String currentStatus = application.getApplicationStatus().toUpperCase();
        String targetStatus = requestDTO.getStatus().trim().toUpperCase();

        // Validate lifecycle transition rules:
        // APPLIED -> SHORTLISTED or REJECTED
        // SHORTLISTED -> SELECTED or REJECTED
        if (!isValidTransition(currentStatus, targetStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, targetStatus);
        }

        application.setApplicationStatus(targetStatus);
        application.setStatusUpdatedDate(LocalDateTime.now());

        if (requestDTO.getEmployerRemarks() != null && !requestDTO.getEmployerRemarks().isBlank()) {
            application.setEmployerRemarks(requestDTO.getEmployerRemarks().trim());
        }

        JobApplication updated = applicationRepository.save(application);
        JobSeekerSummaryDTO candidate = fetchCandidateSafe(updated.getJobSeekerId());
        JobSummaryDTO job = fetchJobSafe(updated.getJobId());

        return mapToResponseDTO(updated, candidate, job);
    }

    @Override
    @Transactional
    public void withdrawApplication(Long id) {
        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        applicationRepository.delete(application);
    }

    // Helper: Validates recruitment state machine transition
    private boolean isValidTransition(String current, String target) {
        if ("APPLIED".equals(current)) {
            return "SHORTLISTED".equals(target) || "REJECTED".equals(target);
        }
        if ("SHORTLISTED".equals(current)) {
            return "SELECTED".equals(target) || "REJECTED".equals(target);
        }
        return false; // SELECTED and REJECTED are terminal states
    }

    // Helper: Verifies candidate exists via Feign (throws 404 if missing)
    private JobSeekerSummaryDTO verifyCandidateExists(Long seekerId) {
        try {
            JobSeekerSummaryDTO candidate = jobSeekerServiceClient.getJobSeekerById(seekerId);
            if (candidate == null) {
                throw new JobSeekerNotFoundException(seekerId);
            }
            return candidate;
        } catch (FeignException.NotFound ex) {
            throw new JobSeekerNotFoundException(seekerId);
        }
    }

    // Helper: Verifies job exists and is ACTIVE via Feign
    private JobSummaryDTO verifyJobExistsAndActive(Long jobId) {
        JobSummaryDTO job;
        try {
            job = jobServiceClient.getJobById(jobId);
            if (job == null) {
                throw new JobNotFoundException(jobId);
            }
        } catch (FeignException.NotFound ex) {
            throw new JobNotFoundException(jobId);
        }

        if (job.getStatus() == null || !"ACTIVE".equalsIgnoreCase(job.getStatus().trim())) {
            throw new JobNotActiveException(jobId, job.getStatus() != null ? job.getStatus() : "UNKNOWN");
        }

        return job;
    }

    // Helper: Safely fetches candidate without failing queries if jobseeker-service
    // is down
    private JobSeekerSummaryDTO fetchCandidateSafe(Long seekerId) {
        try {
            return jobSeekerServiceClient.getJobSeekerById(seekerId);
        } catch (Exception ex) {
            log.warn("Could not retrieve candidate profile for seeker ID {}: {}", seekerId, ex.getMessage());
            return JobSeekerSummaryDTO.builder()
                    .jobSeekerId(seekerId)
                    .firstName("Candidate #" + seekerId)
                    .build();
        }
    }

    // Helper: Safely fetches job opening without failing queries if job-service is
    // down
    private JobSummaryDTO fetchJobSafe(Long jobId) {
        try {
            return jobServiceClient.getJobById(jobId);
        } catch (Exception ex) {
            log.warn("Could not retrieve job opening details for job ID {}: {}", jobId, ex.getMessage());
            return JobSummaryDTO.builder()
                    .jobId(jobId)
                    .jobTitle("Job Opening #" + jobId)
                    .build();
        }
    }

    // Mapper helper
    private ApplicationResponseDTO mapToResponseDTO(JobApplication entity, JobSeekerSummaryDTO candidate,
            JobSummaryDTO job) {
        return ApplicationResponseDTO.builder()
                .applicationId(entity.getApplicationId())
                .jobId(entity.getJobId())
                .jobSeekerId(entity.getJobSeekerId())
                .applicationStatus(entity.getApplicationStatus())
                .resumeUrl(entity.getResumeUrl())
                .employerRemarks(entity.getEmployerRemarks())
                .applicationDate(entity.getApplicationDate())
                .statusUpdatedDate(entity.getStatusUpdatedDate())
                .candidate(candidate)
                .job(job)
                .build();
    }
}
