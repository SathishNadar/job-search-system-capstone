package com.jss.employer.controller;

import com.jss.employer.dto.EmployerRequestDTO;
import com.jss.employer.dto.EmployerResponseDTO;

import com.jss.employer.exception.ErrorResponse;

import com.jss.employer.service.EmployerService;

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
@RequestMapping("/api/employers")
@RequiredArgsConstructor
@Tag(name = "Employer Management", description = "APIs for Employer registration, company profile management, filtering, and searches")
public class EmployerController {

    private final EmployerService employerService;

    @Operation(summary = "Register a new Employer", description = "Creates a new employer organization profile. Enforces duplicate email prevention and field validation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employer successfully registered",
                    content = @Content(schema = @Schema(implementation = EmployerResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error in request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered to another employer",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<EmployerResponseDTO> registerEmployer(@Valid @RequestBody EmployerRequestDTO requestDTO) {
        EmployerResponseDTO created = employerService.registerEmployer(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Get Employer by ID", description = "Fetches a single employer's full profile details using their unique numeric ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employer profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployerResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Employer not found with the requested ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployerResponseDTO> getEmployerById(@PathVariable Long id) {
        EmployerResponseDTO employer = employerService.getEmployerById(id);
        return ResponseEntity.ok(employer);
    }

    @Operation(summary = "Get all Employers (with optional filters)", description = "Retrieves all registered employers, with optional filtering by industry domain and/or city.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of employers retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployerResponseDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<EmployerResponseDTO>> getAllEmployers(
            @RequestParam(required = false) String industryType,
            @RequestParam(required = false) String city) {
        List<EmployerResponseDTO> employers = employerService.getAllEmployers(industryType, city);
        return ResponseEntity.ok(employers);
    }

    @Operation(summary = "Search Employers by Organization Name", description = "Performs a case-insensitive partial name search (e.g. 'Capg' matches 'Capgemini').")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matching employers retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployerResponseDTO.class))))
    })
    @GetMapping("/search")
    public ResponseEntity<List<EmployerResponseDTO>> searchByOrganizationName(
            @RequestParam(required = false) String name) {
        List<EmployerResponseDTO> results = employerService.searchByOrganizationName(name);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Update Employer Profile", description = "Updates an existing employer's profile. Validates that updated email is not claimed by another company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employer updated successfully",
                    content = @Content(schema = @Schema(implementation = EmployerResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error in request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employer not found with the requested ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Updated email conflicts with an existing employer",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployerResponseDTO> updateEmployer(
            @PathVariable Long id,
            @Valid @RequestBody EmployerRequestDTO requestDTO) {
        EmployerResponseDTO updated = employerService.updateEmployer(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete Employer Profile", description = "Permanently removes an employer profile by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employer successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Employer not found with the requested ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEmployer(@PathVariable Long id) {
        employerService.deleteEmployer(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Employer profile with ID " + id + " has been successfully deleted.");
        return ResponseEntity.ok(response);
    }
}
