package com.jss.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Summary of job opening details fetched from job-service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSummaryDTO {

    @Schema(description = "Unique Identifier of the job opening", example = "1")
    private Long jobId;

    @Schema(description = "Job title", example = "Senior Java Developer")
    private String jobTitle;

    @Schema(description = "Designation level", example = "Lead Software Engineer")
    private String designation;

    @Schema(description = "Job location", example = "Bengaluru")
    private String location;

    @Schema(description = "Minimum annual compensation (INR)", example = "800000.0")
    private Double salaryMin;

    @Schema(description = "Maximum annual compensation (INR)", example = "1500000.0")
    private Double salaryMax;

    @Schema(description = "Job lifecycle status", example = "ACTIVE")
    private String status;

    @Schema(description = "ID of the hiring organization", example = "1")
    private Long employerId;
}
