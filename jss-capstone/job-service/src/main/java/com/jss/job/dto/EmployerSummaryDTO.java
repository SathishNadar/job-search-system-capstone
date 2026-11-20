package com.jss.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO representing company details fetched dynamically from
 * 'employer-service'
 * via OpenFeign declarative client.
 */
@Schema(description = "Summary of hiring organization profile fetched from employer-service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerSummaryDTO {

    @Schema(description = "Unique Identifier of the employer", example = "1")
    private Long employerId;

    @Schema(description = "Name of the organization", example = "Capgemini Technology Services")
    private String organizationName;

    @Schema(description = "Primary industry domain", example = "Information Technology")
    private String industryType;

    @Schema(description = "Website URL", example = "https://www.capgemini.com")
    private String website;
}
