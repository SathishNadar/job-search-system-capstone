package com.jss.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Response representation of a conversation message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDTO {

    @Schema(description = "Unique Identifier of the message", example = "1")
    private Long messageId;

    @Schema(description = "Employer ID", example = "1")
    private Long employerId;

    @Schema(description = "Job Seeker ID", example = "1")
    private Long jobSeekerId;

    @Schema(description = "Job Opening ID", example = "1")
    private Long jobId;

    @Schema(description = "Hiring organization name enriched from employer-service", example = "Capgemini Technology Services")
    private String organizationName;

    @Schema(description = "Employer / recruiter contact person name enriched from employer-service", example = "Sundar Pichai")
    private String employerName;

    @Schema(description = "Candidate full name enriched from jobseeker-service", example = "Sathish Nadar")
    private String jobSeekerName;

    @Schema(description = "Author role: EMPLOYER or JOB_SEEKER", example = "EMPLOYER")
    private String senderType;

    @Schema(description = "Optional message subject", example = "Technical Interview Invitation")
    private String subject;

    @Schema(description = "Full message text", example = "Hi Sathish, we would like to schedule Technical Round 1.")
    private String messageDescription;

    @Schema(description = "Timestamp when the message was sent", example = "2026-11-25T10:15:00")
    private LocalDateTime sentDate;

    @Schema(description = "Complete job opening context fetched from job-service (powers the chat '(i)' info popup)")
    private JobSummaryDTO job;
}
