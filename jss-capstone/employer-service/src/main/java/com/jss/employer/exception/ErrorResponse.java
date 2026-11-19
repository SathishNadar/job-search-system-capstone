package com.jss.employer.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard RFC-compliant API Error Response payload.
 * Uniform error envelope returned across all microservices in the Job Search System.
 */
@Schema(description = "Standard API error response format")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2026-11-18T10:15:30")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP error title", example = "Not Found")
    private String error;

    @Schema(description = "Detailed human-readable error description", example = "Employer not found with ID: 99")
    private String message;

    @Schema(description = "Requested URI path", example = "/api/employers/99")
    private String path;
}
