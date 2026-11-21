package com.jss.application.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standard API error response structure")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @Schema(description = "Timestamp of the error", example = "2026-11-23T12:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP reason phrase", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "Job application not found with ID: 1")
    private String message;

    @Schema(description = "Request URI path", example = "/api/applications/1")
    private String path;
}
