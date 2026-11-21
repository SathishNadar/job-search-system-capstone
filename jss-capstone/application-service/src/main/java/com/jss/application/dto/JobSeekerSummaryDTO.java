package com.jss.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Summary of candidate profile fetched from jobseeker-service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerSummaryDTO {

    @Schema(description = "Unique Identifier of the job seeker", example = "1")
    private Long jobSeekerId;

    @Schema(description = "Candidate first name", example = "Sathish")
    private String firstName;

    @Schema(description = "Candidate last name", example = "Nadar")
    private String lastName;

    @Schema(description = "Candidate email address", example = "sathish.nadar@example.com")
    private String email;

    @Schema(description = "Primary contact phone number", example = "9876543210")
    private String contactNumber;

    @Schema(description = "Key technical skills", example = "Java, Spring Boot, Microservices, React")
    private String skills;

    @Schema(description = "Years of relevant work experience", example = "3.5")
    private Double experience;
}
