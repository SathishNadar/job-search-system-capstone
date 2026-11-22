package com.jss.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Lean summary of job seeker fetched from jobseeker-service")
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
}
