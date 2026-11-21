package com.jss.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Response representation of a job application")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponseDTO {

    @Schema(description = "Unique Identifier of the application", example = "1")
    private Long applicationId;

    @Schema(description = "Target job opening ID", example = "1")
    private Long jobId;

    @Schema(description = "Applicant ID", example = "1")
    private Long jobSeekerId;

    @Schema(description = "Current lifecycle status (APPLIED, SHORTLISTED, SELECTED, REJECTED)", example = "APPLIED")
    private String applicationStatus;

    @Schema(description = "Link to candidate's resume", example = "https://storage.jss.com/resumes/sathish_nadar_resume.pdf")
    private String resumeUrl;

    @Schema(description = "Recruiter feedback / interview notes", example = "Profile shortlisted for technical round")
    private String employerRemarks;

    @Schema(description = "Date and time when application was submitted")
    private LocalDateTime applicationDate;

    @Schema(description = "Date and time when status was last updated by recruiter")
    private LocalDateTime statusUpdatedDate;

    @Schema(description = "Enriched candidate profile fetched via OpenFeign from jobseeker-service")
    private JobSeekerSummaryDTO candidate;

    @Schema(description = "Enriched job opening summary fetched via OpenFeign from job-service")
    private JobSummaryDTO job;
}
