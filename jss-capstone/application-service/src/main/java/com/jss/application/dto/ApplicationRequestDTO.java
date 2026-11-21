package com.jss.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Payload submitted by a job seeker to apply for a job")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequestDTO {

    @Schema(description = "Identifier of the target job opening", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Job ID is required")
    private Long jobId;

    @Schema(description = "Identifier of the candidate applying", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Job Seeker ID is required")
    private Long jobSeekerId;

    @Schema(description = "URL / link to candidate's uploaded resume", example = "https://storage.jss.com/resumes/sathish_nadar_resume.pdf")
    private String resumeUrl;
}
