package com.jss.job.controller;

import com.jss.job.dto.JobRequestDTO;
import com.jss.job.dto.JobResponseDTO;
import com.jss.job.exception.ErrorResponse;
import com.jss.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing Job Microservice APIs on port 8084.
 * Fully documented with OpenAPI 3 annotations for interactive testing in Swagger UI.
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Management", description = "APIs for publishing, managing, and searching job openings across employers")
public class JobController {

    private final JobService jobService;

    @Operation(summary = "Publish a new Job opening",
            description = "Creates a new job posting. Verifies that the employer exists via OpenFeign and prevents duplicate active jobs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job opening successfully published",
                    content = @Content(schema = @Schema(implementation = JobResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error in request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employer ID does not exist in employer-service",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Employer already has an active job with this title",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<JobResponseDTO> createJob(@Valid @RequestBody JobRequestDTO requestDTO) {
        JobResponseDTO created = jobService.createJob(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get Job by ID", description = "Fetches complete job opening details, enriched with company information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Job opening not found with requested ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable Long id) {
        JobResponseDTO job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @Operation(summary = "Get all Active Jobs", description = "Returns all jobs currently accepting applications (status = 'ACTIVE').")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of active jobs retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobResponseDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<JobResponseDTO>> getAllActiveJobs() {
        List<JobResponseDTO> jobs = jobService.getAllActiveJobs();
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Unified 5-Factor Candidate Job Search",
            description = "Searches active jobs by any combination of: skill keyword, job title, designation, location, and/or employerId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matching jobs retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobResponseDTO.class))))
    })
    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDTO>> searchJobs(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long employerId) {
        List<JobResponseDTO> results = jobService.searchJobs(skill, jobTitle, designation, location, employerId);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get Jobs by Employer ID",
            description = "Lists all jobs published by a specific company (both ACTIVE and INACTIVE) for the Employer Portal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employer's jobs retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobResponseDTO.class))))
    })
    @GetMapping("/employer/{employerId}")
    public ResponseEntity<List<JobResponseDTO>> getJobsByEmployerId(@PathVariable Long employerId) {
        List<JobResponseDTO> jobs = jobService.getJobsByEmployerId(employerId);
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Update Job Opening", description = "Modifies job details or toggles status (ACTIVE / INACTIVE / CLOSED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job opening updated successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error in request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job or Employer ID not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<JobResponseDTO> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequestDTO requestDTO) {
        JobResponseDTO updated = jobService.updateJob(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Deactivate / Soft-Delete Job",
            description = "Sets the job status to 'INACTIVE' so that candidate application history is preserved while hiding the job from new searches.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job successfully deactivated"),
            @ApiResponse(responseCode = "404", description = "Job opening not found with requested ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Job opening with ID " + id + " has been successfully deactivated (status set to INACTIVE).");
        return ResponseEntity.ok(response);
    }
}
