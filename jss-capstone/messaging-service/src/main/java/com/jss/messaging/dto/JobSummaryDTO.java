package com.jss.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Complete job opening details fetched from job-service for chat context")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSummaryDTO {

    @Schema(description = "Unique Identifier of the job opening", example = "1")
    private Long jobId;

    @Schema(description = "ID of the hiring organization", example = "1")
    private Long employerId;

    @Schema(description = "Title of the job opening", example = "Senior Java Developer")
    private String jobTitle;

    @Schema(description = "Designation level", example = "Lead Software Engineer")
    private String designation;

    @Schema(description = "Job roles and responsibilities")
    private String description;

    @Schema(description = "Required technical skills", example = "Java, Spring Boot, Microservices, MySQL, REST APIs")
    private String requiredSkills;

    @Schema(description = "Required years of experience", example = "3.0")
    private Double experienceRequired;

    @Schema(description = "Educational qualification", example = "B.Tech in Computer Science")
    private String qualification;

    @Schema(description = "Minimum annual compensation (INR)", example = "800000.0")
    private Double salaryMin;

    @Schema(description = "Maximum annual compensation (INR)", example = "1500000.0")
    private Double salaryMax;

    @Schema(description = "Job location", example = "Bengaluru")
    private String location;

    @Schema(description = "Maximum notice period in days", example = "30")
    private Integer noticePeriod;

    @Schema(description = "Contact email for this job opening", example = "careers.india@capgemini.com")
    private String contactEmail;

    @Schema(description = "Employment type", example = "FULL_TIME")
    private String employmentType;

    @Schema(description = "Job lifecycle status", example = "ACTIVE")
    private String status;

    @Schema(description = "Publishing date and time", example = "2026-11-19T10:00:00")
    private LocalDateTime createdDate;
}
