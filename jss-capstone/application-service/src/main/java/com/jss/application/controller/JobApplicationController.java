package com.jss.application.controller;

import com.jss.application.dto.ApplicationRequestDTO;
import com.jss.application.dto.ApplicationResponseDTO;
import com.jss.application.dto.StatusUpdateRequestDTO;
import com.jss.application.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Job Application Management", description = "Endpoints for submitting, tracking, and updating job applications")
public class JobApplicationController {

        private final JobApplicationService applicationService;

        @Operation(summary = "Submit a job application", description = "Validates candidate and active job opening via OpenFeign, checks duplicate application, and sets status to APPLIED.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Application successfully submitted"),
                        @ApiResponse(responseCode = "400", description = "Validation error or target job is not in ACTIVE status"),
                        @ApiResponse(responseCode = "404", description = "Candidate or Job opening not found"),
                        @ApiResponse(responseCode = "409", description = "Candidate has already applied for this job opening")
        })
        @PostMapping
        public ResponseEntity<ApplicationResponseDTO> applyForJob(
                        @Valid @RequestBody ApplicationRequestDTO requestDTO) {
                ApplicationResponseDTO response = applicationService.applyForJob(requestDTO);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        @Operation(summary = "Get application by ID", description = "Retrieves application record enriched with candidate profile and job details.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Application found"),
                        @ApiResponse(responseCode = "404", description = "Application ID not found")
        })
        @GetMapping("/{id}")
        public ResponseEntity<ApplicationResponseDTO> getApplicationById(
                        @Parameter(description = "Application ID", example = "1") @PathVariable Long id) {
                return ResponseEntity.ok(applicationService.getApplicationById(id));
        }

        @Operation(summary = "Candidate Portal: View my applications", description = "Retrieves all job applications submitted by a specific candidate.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully")
        })
        @GetMapping("/jobseeker/{jobSeekerId}")
        public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByJobSeekerId(
                        @Parameter(description = "Job Seeker ID", example = "1") @PathVariable Long jobSeekerId) {
                return ResponseEntity.ok(applicationService.getApplicationsByJobSeekerId(jobSeekerId));
        }

        @Operation(summary = "Recruiter Portal: View applicants for a job", description = "Retrieves all applications submitted for a specific job opening.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Applicants retrieved successfully")
        })
        @GetMapping("/job/{jobId}")
        public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByJobId(
                        @Parameter(description = "Job ID", example = "1") @PathVariable Long jobId) {
                return ResponseEntity.ok(applicationService.getApplicationsByJobId(jobId));
        }

        @Operation(summary = "Recruiter Filter: View applicants by status", description = "Filters applicants for a job opening by recruitment status (e.g. SHORTLISTED, SELECTED).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Filtered applicants retrieved successfully")
        })
        @GetMapping("/job/{jobId}/status/{status}")
        public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByJobIdAndStatus(
                        @Parameter(description = "Job ID", example = "1") @PathVariable Long jobId,
                        @Parameter(description = "Target status (SHORTLISTED, SELECTED, REJECTED, APPLIED)", example = "SHORTLISTED") @PathVariable String status) {
                return ResponseEntity.ok(applicationService.getApplicationsByJobIdAndStatus(jobId, status));
        }

        @Operation(summary = "Recruiter Action: Update application status", description = "Partially updates an application's status (SHORTLISTED, SELECTED, REJECTED) with optional remarks.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid status transition attempted"),
                        @ApiResponse(responseCode = "404", description = "Application not found")
        })
        @PatchMapping("/{id}/status")
        public ResponseEntity<ApplicationResponseDTO> updateApplicationStatus(
                        @Parameter(description = "Application ID", example = "1") @PathVariable Long id,
                        @Valid @RequestBody StatusUpdateRequestDTO requestDTO) {
                return ResponseEntity.ok(applicationService.updateApplicationStatus(id, requestDTO));
        }

        @Operation(summary = "Withdraw application", description = "Allows a candidate to withdraw an application before recruiter review.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Application withdrawn successfully"),
                        @ApiResponse(responseCode = "404", description = "Application not found")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Map<String, String>> withdrawApplication(
                        @Parameter(description = "Application ID", example = "1") @PathVariable Long id) {
                applicationService.withdrawApplication(id);
                return ResponseEntity.ok(Collections.singletonMap("message",
                                "Job application ID " + id + " has been successfully withdrawn."));
        }
}
