package com.jss.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Schema(description = "Employer identity summary fetched from employer-service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerSummaryDTO {

    @Schema(description = "Unique Identifier of the employer", example = "1")
    private Long employerId;

    @Schema(description = "Organization name displayed in conversation", example = "Capgemini Technology Services")
    private String organizationName;

    @Schema(description = "Employer / recruiter contact person name", example = "Sathish Nadar")
    private String contactPerson;
}
