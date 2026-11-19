package com.jss.employer.controller;

import com.jss.employer.dto.EmployerRequestDTO;
import com.jss.employer.dto.EmployerResponseDTO;
import com.jss.employer.exception.ErrorResponse;
import com.jss.employer.service.EmployerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employers")
@Tag(name = "Employer Management", description = "APIs for Employer Registration, Profile Management, and Candidate Search Operations")
public class EmployerController {

        private final EmployerService employerService;

        public EmployerController(EmployerService employerService) {
                this.employerService = employerService;
        }

        @PostMapping
        @Operation(summary = "Register a new Employer", description = "Creates a new employer profile in the system with validation.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Employer successfully registered", content = @Content(schema = @Schema(implementation = EmployerResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error in request payload", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Employer with email already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<EmployerResponseDTO> registerEmployer(
                        @Valid @RequestBody EmployerRequestDTO requestDTO) {
                EmployerResponseDTO createdEmployer = employerService.registerEmployer(requestDTO);
                return new ResponseEntity<>(createdEmployer, HttpStatus.CREATED);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get Employer by ID", description = "Retrieves an employer profile by unique employer ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Employer found", content = @Content(schema = @Schema(implementation = EmployerResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Employer not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<EmployerResponseDTO> getEmployerById(@PathVariable("id") Long id) {
                EmployerResponseDTO employer = employerService.getEmployerById(id);
                return ResponseEntity.ok(employer);
        }

        @GetMapping
        @Operation(summary = "Get All Employers / Search by Filter", description = "Retrieves all employers or filters by industry type and city.")
        @ApiResponse(responseCode = "200", description = "List of employers retrieved successfully")
        public ResponseEntity<List<EmployerResponseDTO>> getAllEmployers(
                        @RequestParam(value = "industryType", required = false) String industryType,
                        @RequestParam(value = "city", required = false) String city) {
                List<EmployerResponseDTO> employers = employerService.getAllEmployers(industryType, city);
                return ResponseEntity.ok(employers);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update Employer Profile", description = "Updates an existing employer's profile details.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Employer profile successfully updated", content = @Content(schema = @Schema(implementation = EmployerResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Employer not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<EmployerResponseDTO> updateEmployer(
                        @PathVariable("id") Long id,
                        @Valid @RequestBody EmployerRequestDTO requestDTO) {
                EmployerResponseDTO updatedEmployer = employerService.updateEmployer(id, requestDTO);
                return ResponseEntity.ok(updatedEmployer);
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete Employer", description = "Deletes an employer profile by ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Employer successfully deleted"),
                        @ApiResponse(responseCode = "404", description = "Employer not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<Map<String, String>> deleteEmployer(@PathVariable("id") Long id) {
                employerService.deleteEmployer(id);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Employer with id " + id + " has been successfully deleted");
                return ResponseEntity.ok(response);
        }
}
