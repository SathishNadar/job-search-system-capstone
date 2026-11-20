package com.jss.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or updating a Job opening.
 * Incorporates Bean Validation annotations and OpenAPI @Schema examples for instant 1-click execution.
 */
@Schema(description = "Request body payload for publishing or updating a job opening")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequestDTO {

    @NotNull(message = "Employer ID cannot be null")
    @Schema(description = "ID of the hiring organization (must exist in employer-service)", example = "1")
    private Long employerId;

    @NotBlank(message = "Job title cannot be blank")
    @Size(min = 2, max = 100, message = "Job title must be between 2 and 100 characters")
    @Schema(description = "Official job opening title", example = "Senior Java Developer")
    private String jobTitle;

    @Schema(description = "Designation level", example = "Lead Software Engineer")
    private String designation;

    @NotBlank(message = "Job description cannot be blank")
    @Size(min = 10, max = 2000, message = "Job description must be between 10 and 2000 characters")
    @Schema(description = "Detailed job roles, responsibilities, and team overview",
            example = "Looking for an experienced Senior Java Developer to design and develop enterprise microservices using Spring Boot, Kafka, and MySQL.")
    private String description;

    @NotBlank(message = "Required skills cannot be blank")
    @Schema(description = "Comma-separated list of required technical competencies",
            example = "Java, Spring Boot, Microservices, MySQL, Docker, REST APIs")
    private String requiredSkills;

    @NotNull(message = "Required experience cannot be null")
    @PositiveOrZero(message = "Required experience cannot be negative")
    @Schema(description = "Minimum years of relevant experience required", example = "3.0")
    private Double experienceRequired;

    @Schema(description = "Required educational qualification", example = "B.Tech / B.E. in Computer Science or related degree")
    private String qualification;

    @NotNull(message = "Minimum salary cannot be null")
    @Positive(message = "Minimum salary must be greater than zero")
    @Schema(description = "Minimum annual compensation in INR", example = "800000.0")
    private Double salaryMin;

    @NotNull(message = "Maximum salary cannot be null")
    @Positive(message = "Maximum salary must be greater than zero")
    @Schema(description = "Maximum annual compensation in INR", example = "1500000.0")
    private Double salaryMax;

    @NotBlank(message = "Job location cannot be blank")
    @Schema(description = "Work location or remote policy", example = "Bengaluru")
    private String location;

    @NotNull(message = "Notice period cannot be null")
    @PositiveOrZero(message = "Notice period in days cannot be negative")
    @Schema(description = "Maximum acceptable candidate notice period in days", example = "30")
    private Integer noticePeriod;

    @NotBlank(message = "Contact email cannot be blank")
    @Email(message = "Contact email must be a valid email format")
    @Schema(description = "Recruitment contact email for applications", example = "careers.india@capgemini.com")
    private String contactEmail;

    @NotBlank(message = "Employment type cannot be blank")
    @Schema(description = "Employment type (FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP)", example = "FULL_TIME")
    private String employmentType;

    @Schema(description = "Current job status (ACTIVE, INACTIVE, CLOSED)", example = "ACTIVE")
    private String status;
}
