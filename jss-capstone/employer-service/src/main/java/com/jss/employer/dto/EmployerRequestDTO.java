package com.jss.employer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Request DTO for creating or updating an Employer profile.
 * Incorporates Bean Validation annotations and OpenAPI @Schema examples for
 * instant 1-click execution.
 */
@Schema(description = "Request body payload for registering or updating an employer profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerRequestDTO {

    @NotBlank(message = "Organization name cannot be blank")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    @Schema(description = "Name of the hiring organization", example = "Google India Pvt Ltd")
    private String organizationName;

    @NotBlank(message = "Contact person name cannot be blank")
    @Size(min = 2, max = 50, message = "Contact person name must be between 2 and 50 characters")
    @Schema(description = "Primary contact representative", example = "Sundar Pichai")
    private String contactPerson;

    @Schema(description = "Street address of the organization", example = "RMZ Infinity, Old Madras Road")
    private String address;

    @NotBlank(message = "City cannot be blank")
    @Schema(description = "City where the office is located", example = "Bengaluru")
    private String city;

    @Schema(description = "State where the office is located", example = "Karnataka")
    private String state;

    @Schema(description = "Country", example = "India")
    private String country;

    @NotBlank(message = "Contact number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be a valid 10-digit phone number")
    @Schema(description = "10-digit mobile or telephone number", example = "9876543210")
    private String contactNumber;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email format")
    @Schema(description = "Official organization email address", example = "careers@google.com")
    private String email;

    @Schema(description = "Official website URL", example = "https://careers.google.com")
    private String website;

    @NotBlank(message = "Industry type cannot be blank")
    @Schema(description = "Primary industry domain", example = "Information Technology")
    private String industryType;

    @Schema(description = "Company size range", example = "10000+ Employees")
    private String companySize;
}
