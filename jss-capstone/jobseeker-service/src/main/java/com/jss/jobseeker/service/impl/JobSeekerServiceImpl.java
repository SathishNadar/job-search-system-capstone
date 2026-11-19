package com.jss.jobseeker.service.impl;

import com.jss.jobseeker.dto.JobSeekerRequestDTO;
import com.jss.jobseeker.dto.JobSeekerResponseDTO;
import com.jss.jobseeker.entity.JobSeeker;
import com.jss.jobseeker.exception.DuplicateJobSeekerException;
import com.jss.jobseeker.exception.JobSeekerNotFoundException;
import com.jss.jobseeker.repository.JobSeekerRepository;
import com.jss.jobseeker.service.JobSeekerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of JobSeekerService business logic.
 * Handles validation, persistence into MySQL, and DTO-Entity conversions using Lombok builders.
 */
@Service
@RequiredArgsConstructor
public class JobSeekerServiceImpl implements JobSeekerService {

    private final JobSeekerRepository jobSeekerRepository;

    @Override
    @Transactional
    public JobSeekerResponseDTO registerJobSeeker(JobSeekerRequestDTO requestDTO) {
        if (jobSeekerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateJobSeekerException("Candidate already registered with email: " + requestDTO.getEmail());
        }

        JobSeeker candidate = mapToEntity(requestDTO);
        JobSeeker savedCandidate = jobSeekerRepository.save(candidate);

        return mapToResponseDTO(savedCandidate);
    }

    @Override
    @Transactional(readOnly = true)
    public JobSeekerResponseDTO getJobSeekerById(Long id) {
        JobSeeker candidate = jobSeekerRepository.findById(id)
                .orElseThrow(() -> new JobSeekerNotFoundException(id));
        return mapToResponseDTO(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobSeekerResponseDTO> getAllJobSeekers() {
        return jobSeekerRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobSeekerResponseDTO> searchCandidates(String skill, String city, Double minExperience) {
        String cleanSkill = (skill != null && !skill.trim().isEmpty()) ? skill.trim() : null;
        String cleanCity = (city != null && !city.trim().isEmpty()) ? city.trim() : null;

        // If no criteria are provided, return the full list
        if (cleanSkill == null && cleanCity == null && minExperience == null) {
            return getAllJobSeekers();
        }

        return jobSeekerRepository.searchCandidates(cleanSkill, cleanCity, minExperience).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobSeekerResponseDTO updateJobSeeker(Long id, JobSeekerRequestDTO requestDTO) {
        JobSeeker existingCandidate = jobSeekerRepository.findById(id)
                .orElseThrow(() -> new JobSeekerNotFoundException(id));

        // Smart duplicate check: only validate if email is actually changed
        if (!existingCandidate.getEmail().equalsIgnoreCase(requestDTO.getEmail())) {
            jobSeekerRepository.findByEmail(requestDTO.getEmail()).ifPresent(found -> {
                if (!found.getJobSeekerId().equals(id)) {
                    throw new DuplicateJobSeekerException(requestDTO.getEmail(), found.getJobSeekerId());
                }
            });
        }

        existingCandidate.setFirstName(requestDTO.getFirstName());
        existingCandidate.setLastName(requestDTO.getLastName());
        existingCandidate.setEmail(requestDTO.getEmail());
        existingCandidate.setContactNumber(requestDTO.getContactNumber());
        existingCandidate.setAddress(requestDTO.getAddress());
        existingCandidate.setCity(requestDTO.getCity());
        existingCandidate.setState(requestDTO.getState());
        existingCandidate.setTotalExperience(requestDTO.getTotalExperience());
        existingCandidate.setCurrentDesignation(requestDTO.getCurrentDesignation());
        existingCandidate.setExpectedCTC(requestDTO.getExpectedCTC());
        existingCandidate.setNoticePeriod(requestDTO.getNoticePeriod());
        existingCandidate.setSkills(requestDTO.getSkills());
        existingCandidate.setQualification(requestDTO.getQualification());

        JobSeeker updatedCandidate = jobSeekerRepository.save(existingCandidate);
        return mapToResponseDTO(updatedCandidate);
    }

    @Override
    @Transactional
    public void deleteJobSeeker(Long id) {
        JobSeeker existingCandidate = jobSeekerRepository.findById(id)
                .orElseThrow(() -> new JobSeekerNotFoundException(id));

        /*
         * FUTURE EXTENSION POINT (Step 4):
         * When 'application-service' and 'ApplicationServiceClient' (OpenFeign) are integrated:
         *
         * long activeAppsCount = applicationServiceClient.countActiveApplicationsByJobSeekerId(id);
         * if (activeAppsCount > 0) {
         *     throw new CannotDeleteJobSeekerWithActiveApplicationsException(id, activeAppsCount);
         * }
         */

        jobSeekerRepository.delete(existingCandidate);
    }

    // Helper mapping methods using Lombok @Builder
    private JobSeeker mapToEntity(JobSeekerRequestDTO dto) {
        return JobSeeker.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .contactNumber(dto.getContactNumber())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .totalExperience(dto.getTotalExperience())
                .currentDesignation(dto.getCurrentDesignation())
                .expectedCTC(dto.getExpectedCTC())
                .noticePeriod(dto.getNoticePeriod())
                .skills(dto.getSkills())
                .qualification(dto.getQualification())
                .build();
    }

    private JobSeekerResponseDTO mapToResponseDTO(JobSeeker entity) {
        return JobSeekerResponseDTO.builder()
                .jobSeekerId(entity.getJobSeekerId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .contactNumber(entity.getContactNumber())
                .address(entity.getAddress())
                .city(entity.getCity())
                .state(entity.getState())
                .totalExperience(entity.getTotalExperience())
                .currentDesignation(entity.getCurrentDesignation())
                .expectedCTC(entity.getExpectedCTC())
                .noticePeriod(entity.getNoticePeriod())
                .skills(entity.getSkills())
                .qualification(entity.getQualification())
                .createdDate(entity.getCreatedDate())
                .build();
    }
}
