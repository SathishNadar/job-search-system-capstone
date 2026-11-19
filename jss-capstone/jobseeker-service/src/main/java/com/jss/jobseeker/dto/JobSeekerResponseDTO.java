package com.jss.jobseeker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Response representation of a candidate profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerResponseDTO {

    @Schema(description = "Unique Identifier of the candidate", example = "1")
    private Long jobSeekerId;

    @Schema(description = "First name", example = "Sathish")
    private String firstName;

    @Schema(description = "Last name", example = "Nadar")
    private String lastName;

    @Schema(description = "Email address", example = "sathish.nadar@example.com")
    private String email;

    @Schema(description = "10-digit contact number", example = "9876543210")
    private String contactNumber;

    @Schema(description = "Street address", example = "Flat 402, Sunshine Heights, Powai")
    private String address;

    @Schema(description = "Current city", example = "Mumbai")
    private String city;

    @Schema(description = "Current state", example = "Maharashtra")
    private String state;

    @Schema(description = "Total experience in years", example = "3.5")
    private Double totalExperience;

    @Schema(description = "Current job title", example = "Software Engineer")
    private String currentDesignation;

    @Schema(description = "Expected annual compensation (CTC)", example = "1200000.0")
    private Double expectedCTC;

    @Schema(description = "Notice period in days", example = "30")
    private Integer noticePeriod;

    @Schema(description = "Technical skills", example = "Java, Spring Boot, Microservices, MySQL, REST APIs")
    private String skills;

    @Schema(description = "Highest qualification", example = "B.Tech in Computer Science")
    private String qualification;

    @Schema(description = "Registration date and time", example = "2026-11-19T10:00:00")
    private LocalDateTime createdDate;
}
