package com.jss.jobseeker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request body payload for registering or updating a candidate profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerRequestDTO {

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Schema(description = "Candidate's first name", example = "Sathish")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Schema(description = "Candidate's last name", example = "Nadar")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email format")
    @Schema(description = "Candidate's primary email address", example = "sathish.nadar@example.com")
    private String email;

    @NotBlank(message = "Contact number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be a valid 10-digit phone number")
    @Schema(description = "10-digit mobile phone number", example = "9876543210")
    private String contactNumber;

    @Schema(description = "Street address", example = "Flat 402, Sunshine Heights, Powai")
    private String address;

    @NotBlank(message = "City cannot be blank")
    @Schema(description = "Current residential city", example = "Mumbai")
    private String city;

    @Schema(description = "Current residential state", example = "Maharashtra")
    private String state;

    @NotNull(message = "Total experience cannot be null")
    @PositiveOrZero(message = "Total experience cannot be negative")
    @Schema(description = "Total work experience in years", example = "3.5")
    private Double totalExperience;

    @Schema(description = "Current professional title / designation", example = "Software Engineer")
    private String currentDesignation;

    @NotNull(message = "Expected CTC cannot be null")
    @Positive(message = "Expected CTC must be greater than zero")
    @Schema(description = "Expected annual compensation in INR", example = "1200000.0")
    private Double expectedCTC;

    @NotNull(message = "Notice period cannot be null")
    @PositiveOrZero(message = "Notice period in days cannot be negative (0 for immediate joiners)")
    @Schema(description = "Notice period duration in days", example = "30")
    private Integer noticePeriod;

    @NotBlank(message = "Skills cannot be blank")
    @Schema(description = "Comma-separated technical and professional skills", example = "Java, Spring Boot, Microservices, MySQL, REST APIs")
    private String skills;

    @Schema(description = "Highest educational qualification", example = "B.Tech in Computer Science")
    private String qualification;
}
