package com.jss.jobseeker.controller;

import com.jss.jobseeker.dto.JobSeekerRequestDTO;
import com.jss.jobseeker.dto.JobSeekerResponseDTO;
import com.jss.jobseeker.exception.ErrorResponse;
import com.jss.jobseeker.service.JobSeekerService;
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

@RestController
@RequestMapping("/api/jobseekers")
@RequiredArgsConstructor
@Tag(name = "Job Seeker Management", description = "APIs for candidate registration, profile management, and unified candidate search")
public class JobSeekerController {

        private final JobSeekerService jobSeekerService;

        @Operation(summary = "Register a new Job Seeker", description = "Creates a new candidate profile. Enforces duplicate email prevention and field validations.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Candidate successfully registered", content = @Content(schema = @Schema(implementation = JobSeekerResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error in request body", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Email already registered to another candidate", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PostMapping
        public ResponseEntity<JobSeekerResponseDTO> registerJobSeeker(
                        @Valid @RequestBody JobSeekerRequestDTO requestDTO) {
                JobSeekerResponseDTO created = jobSeekerService.registerJobSeeker(requestDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        @Operation(summary = "Get Candidate by ID", description = "Fetches a single candidate's full profile using their unique numeric ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Candidate profile retrieved successfully", content = @Content(schema = @Schema(implementation = JobSeekerResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Candidate not found with requested ID", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{id}")
        public ResponseEntity<JobSeekerResponseDTO> getJobSeekerById(@PathVariable Long id) {
                JobSeekerResponseDTO candidate = jobSeekerService.getJobSeekerById(id);
                return ResponseEntity.ok(candidate);
        }

        @Operation(summary = "Get all Candidates", description = "Retrieves all registered candidates in the system (simple directory listing).")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "List of candidates retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobSeekerResponseDTO.class))))
        })
        @GetMapping
        public ResponseEntity<List<JobSeekerResponseDTO>> getAllJobSeekers() {
                List<JobSeekerResponseDTO> candidates = jobSeekerService.getAllJobSeekers();
                return ResponseEntity.ok(candidates);
        }

        @Operation(summary = "Search Candidates by Skills, Location, and Experience", description = "Unified search and filter endpoint for recruiters: filter candidates by partial skill keyword, city, and/or minimum years of experience.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Matching candidates retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobSeekerResponseDTO.class))))
        })
        @GetMapping("/search")
        public ResponseEntity<List<JobSeekerResponseDTO>> searchCandidates(
                        @RequestParam(required = false) String skill,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) Double minExperience) {
                List<JobSeekerResponseDTO> results = jobSeekerService.searchCandidates(skill, city, minExperience);
                return ResponseEntity.ok(results);
        }

        @Operation(summary = "Update Candidate Profile", description = "Updates an existing candidate's profile. Validates that updated email is not claimed by another candidate.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Candidate updated successfully", content = @Content(schema = @Schema(implementation = JobSeekerResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error in request body", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Candidate not found with requested ID", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Updated email conflicts with an existing candidate", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PutMapping("/{id}")
        public ResponseEntity<JobSeekerResponseDTO> updateJobSeeker(
                        @PathVariable Long id,
                        @Valid @RequestBody JobSeekerRequestDTO requestDTO) {
                JobSeekerResponseDTO updated = jobSeekerService.updateJobSeeker(id, requestDTO);
                return ResponseEntity.ok(updated);
        }

        @Operation(summary = "Delete Candidate Profile", description = "Permanently removes a candidate profile by ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Candidate successfully deleted"),
                        @ApiResponse(responseCode = "404", description = "Candidate not found with requested ID", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Map<String, String>> deleteJobSeeker(@PathVariable Long id) {
                jobSeekerService.deleteJobSeeker(id);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Candidate profile with ID " + id + " has been successfully deleted.");
                return ResponseEntity.ok(response);
        }
}
