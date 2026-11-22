package com.jss.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Schema(description = "Payload to send a message in a job-specific conversation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequestDTO {

    @Schema(description = "Employer ID participating in the conversation", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Employer ID is required")
    private Long employerId;

    @Schema(description = "Job Seeker ID participating in the conversation", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Job Seeker ID is required")
    private Long jobSeekerId;

    @Schema(description = "Job Opening ID contextualizing the conversation", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Job ID is required")
    private Long jobId;

    @Schema(description = "Identifies the sender of the message: EMPLOYER or JOB_SEEKER", example = "EMPLOYER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Sender type is required")
    @Pattern(regexp = "^(EMPLOYER|JOB_SEEKER)$", message = "Sender type must be either 'EMPLOYER' or 'JOB_SEEKER'")
    private String senderType;

    @Schema(description = "Optional subject line of the message (can be omitted for casual chat replies)", example = "Technical Interview Schedule", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;

    @Schema(description = "Full message content", example = "Hi Sathish, are you available this Thursday at 11 AM for Technical Round 1?", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Message description is required")
    @Size(max = 3000, message = "Message description must not exceed 3000 characters")
    private String messageDescription;
}
