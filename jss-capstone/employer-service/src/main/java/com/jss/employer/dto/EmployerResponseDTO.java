package com.jss.employer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO returned for all Employer queries and mutations.
 * Fully annotated with OpenAPI @Schema for interactive Swagger documentation.
 */
@Schema(description = "Response representation of an Employer profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerResponseDTO {

    @Schema(description = "Unique Identifier of the employer", example = "1")
    private Long employerId;

    @Schema(description = "Name of the organization", example = "Google India Pvt Ltd")
    private String organizationName;

    @Schema(description = "Primary contact person", example = "Sundar Pichai")
    private String contactPerson;

    @Schema(description = "Street address", example = "RMZ Infinity, Old Madras Road")
    private String address;

    @Schema(description = "City", example = "Bengaluru")
    private String city;

    @Schema(description = "State", example = "Karnataka")
    private String state;

    @Schema(description = "Country", example = "India")
    private String country;

    @Schema(description = "Contact phone number", example = "9876543210")
    private String contactNumber;

    @Schema(description = "Organization email", example = "careers@google.com")
    private String email;

    @Schema(description = "Website URL", example = "https://careers.google.com")
    private String website;

    @Schema(description = "Industry domain", example = "Information Technology")
    private String industryType;

    @Schema(description = "Company size range", example = "10000+ Employees")
    private String companySize;

    @Schema(description = "Registration date and time", example = "2026-11-16T10:00:00")
    private LocalDateTime createdDate;
}
