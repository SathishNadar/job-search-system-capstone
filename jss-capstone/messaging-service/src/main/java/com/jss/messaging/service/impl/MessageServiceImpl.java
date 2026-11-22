package com.jss.messaging.service.impl;

import com.jss.messaging.client.EmployerServiceClient;
import com.jss.messaging.client.JobSeekerServiceClient;
import com.jss.messaging.client.JobServiceClient;
import com.jss.messaging.dto.*;
import com.jss.messaging.entity.Message;
import com.jss.messaging.exception.EmployerNotFoundException;
import com.jss.messaging.exception.JobNotFoundException;
import com.jss.messaging.exception.JobSeekerNotFoundException;
import com.jss.messaging.repository.MessageRepository;
import com.jss.messaging.service.MessageService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final EmployerServiceClient employerServiceClient;
    private final JobSeekerServiceClient jobSeekerServiceClient;
    private final JobServiceClient jobServiceClient;

    @Override
    @Transactional
    public MessageResponseDTO sendMessage(MessageRequestDTO requestDTO) {
        // 1. Validate all three participants exist via OpenFeign
        EmployerSummaryDTO employer = verifyEmployerExists(requestDTO.getEmployerId());
        JobSeekerSummaryDTO seeker = verifyJobSeekerExists(requestDTO.getJobSeekerId());
        JobSummaryDTO job = verifyJobExists(requestDTO.getJobId());

        String cleanSubject = (requestDTO.getSubject() != null && !requestDTO.getSubject().isBlank())
                ? requestDTO.getSubject().trim()
                : null;

        // 2. Persist unified message record to MySQL
        Message message = Message.builder()
                .employerId(requestDTO.getEmployerId())
                .jobSeekerId(requestDTO.getJobSeekerId())
                .jobId(requestDTO.getJobId())
                .senderType(requestDTO.getSenderType().trim())
                .subject(cleanSubject)
                .messageDescription(requestDTO.getMessageDescription().trim())
                .sentDate(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);
        return mapToResponseDTO(saved, employer, seeker, job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getConversation(Long jobId, Long employerId, Long jobSeekerId) {
        List<Message> messages = messageRepository
                .findByJobIdAndEmployerIdAndJobSeekerIdOrderBySentDateAsc(jobId, employerId, jobSeekerId);

        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch participant context once for the entire thread (prevents N+1 Feign calls)
        EmployerSummaryDTO employer = fetchEmployerSafe(employerId);
        JobSeekerSummaryDTO seeker = fetchJobSeekerSafe(jobSeekerId);
        JobSummaryDTO job = fetchJobSafe(jobId);

        return messages.stream()
                .map(msg -> mapToResponseDTO(msg, employer, seeker, job))
                .collect(Collectors.toList());
    }

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

    private JobSeekerSummaryDTO verifyJobSeekerExists(Long jobSeekerId) {
        try {
            JobSeekerSummaryDTO seeker = jobSeekerServiceClient.getJobSeekerById(jobSeekerId);
            if (seeker == null) {
                throw new JobSeekerNotFoundException(jobSeekerId);
            }
            return seeker;
        } catch (FeignException.NotFound ex) {
            throw new JobSeekerNotFoundException(jobSeekerId);
        }
    }

    private JobSummaryDTO verifyJobExists(Long jobId) {
        try {
            JobSummaryDTO job = jobServiceClient.getJobById(jobId);
            if (job == null) {
                throw new JobNotFoundException(jobId);
            }
            return job;
        } catch (FeignException.NotFound ex) {
            throw new JobNotFoundException(jobId);
        }
    }

    private EmployerSummaryDTO fetchEmployerSafe(Long employerId) {
        try {
            return employerServiceClient.getEmployerById(employerId);
        } catch (Exception ex) {
            log.warn("Could not retrieve employer profile for ID {}: {}", employerId, ex.getMessage());
            return EmployerSummaryDTO.builder()
                    .employerId(employerId)
                    .organizationName("Organization #" + employerId)
                    .contactPerson("Recruiter")
                    .build();
        }
    }

    private JobSeekerSummaryDTO fetchJobSeekerSafe(Long jobSeekerId) {
        try {
            return jobSeekerServiceClient.getJobSeekerById(jobSeekerId);
        } catch (Exception ex) {
            log.warn("Could not retrieve candidate name for ID {}: {}", jobSeekerId, ex.getMessage());
            return JobSeekerSummaryDTO.builder()
                    .jobSeekerId(jobSeekerId)
                    .firstName("Candidate")
                    .lastName("#" + jobSeekerId)
                    .build();
        }
    }

    private JobSummaryDTO fetchJobSafe(Long jobId) {
        try {
            return jobServiceClient.getJobById(jobId);
        } catch (Exception ex) {
            log.warn("Could not retrieve job details for ID {}: {}", jobId, ex.getMessage());
            return JobSummaryDTO.builder()
                    .jobId(jobId)
                    .jobTitle("Job #" + jobId)
                    .build();
        }
    }

    private MessageResponseDTO mapToResponseDTO(
            Message entity,
            EmployerSummaryDTO employer,
            JobSeekerSummaryDTO seeker,
            JobSummaryDTO job) {
        String organizationName = (employer != null && employer.getOrganizationName() != null)
                ? employer.getOrganizationName()
                : "Organization #" + entity.getEmployerId();

        String employerName = (employer != null && employer.getContactPerson() != null)
                ? employer.getContactPerson()
                : "Recruiter";

        String seekerName = (seeker != null && seeker.getFirstName() != null)
                ? (seeker.getFirstName() + (seeker.getLastName() != null ? " " + seeker.getLastName() : "")).trim()
                : "Candidate #" + entity.getJobSeekerId();

        return MessageResponseDTO.builder()
                .messageId(entity.getMessageId())
                .employerId(entity.getEmployerId())
                .organizationName(organizationName)
                .employerName(employerName)
                .jobSeekerId(entity.getJobSeekerId())
                .jobSeekerName(seekerName)
                .jobId(entity.getJobId())
                .senderType(entity.getSenderType())
                .subject(entity.getSubject())
                .messageDescription(entity.getMessageDescription())
                .sentDate(entity.getSentDate())
                .job(job)
                .build();
    }
}
