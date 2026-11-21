package com.jss.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Payload submitted by recruiter to update application status and provide feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequestDTO {

    @Schema(description = "New application status (SHORTLISTED, SELECTED, or REJECTED)", example = "SHORTLISTED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Status is required")
    private String status;

    @Schema(description = "Optional feedback or interview notes provided by recruiter", example = "Profile shortlisted for Technical Round 1")
    private String employerRemarks;
}
