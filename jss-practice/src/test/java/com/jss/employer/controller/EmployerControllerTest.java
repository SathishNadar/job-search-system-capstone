package com.jss.employer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jss.employer.dto.EmployerRequestDTO;
import com.jss.employer.dto.EmployerResponseDTO;
import com.jss.employer.exception.EmployerNotFoundException;
import com.jss.employer.service.EmployerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployerController.class)
class EmployerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployerService employerService;

    private EmployerResponseDTO sampleResponse;
    private EmployerRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = new EmployerResponseDTO(
                1L,
                "Google India Pvt Ltd",
                "Sundar Pichai",
                "RMZ Infinity",
                "Bengaluru",
                "Karnataka",
                "India",
                "9876543210",
                "careers@google.com",
                "https://careers.google.com",
                "Information Technology",
                "10000+ Employees",
                LocalDateTime.now()
        );

        validRequest = new EmployerRequestDTO(
                "Google India Pvt Ltd",
                "Sundar Pichai",
                "RMZ Infinity",
                "Bengaluru",
                "Karnataka",
                "India",
                "9876543210",
                "careers@google.com",
                "https://careers.google.com",
                "Information Technology",
                "10000+ Employees"
        );
    }

    @Test
    @DisplayName("POST /api/employers - Success returns 201 Created")
    void testRegisterEmployer_Success() throws Exception {
        when(employerService.registerEmployer(any(EmployerRequestDTO.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/employers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employerId").value(1L))
                .andExpect(jsonPath("$.organizationName").value("Google India Pvt Ltd"))
                .andExpect(jsonPath("$.email").value("careers@google.com"));
    }

    @Test
    @DisplayName("POST /api/employers - Validation Failure returns 400 Bad Request")
    void testRegisterEmployer_ValidationFailure() throws Exception {
        EmployerRequestDTO invalidRequest = new EmployerRequestDTO();
        invalidRequest.setOrganizationName(""); // blank -> should fail validation
        invalidRequest.setEmail("not-an-email"); // invalid email pattern
        invalidRequest.setContactNumber("123"); // invalid phone number length

        mockMvc.perform(post("/api/employers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/employers"));
    }

    @Test
    @DisplayName("GET /api/employers/{id} - Success returns 200 OK")
    void testGetEmployerById_Success() throws Exception {
        when(employerService.getEmployerById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/employers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employerId").value(1L))
                .andExpect(jsonPath("$.organizationName").value("Google India Pvt Ltd"));
    }

    @Test
    @DisplayName("GET /api/employers/{id} - Not Found returns 404")
    void testGetEmployerById_NotFound() throws Exception {
        when(employerService.getEmployerById(999L)).thenThrow(new EmployerNotFoundException(999L));

        mockMvc.perform(get("/api/employers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Employer not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/employers/999"));
    }

    @Test
    @DisplayName("GET /api/employers - Success returns 200 and list")
    void testGetAllEmployers() throws Exception {
        when(employerService.getAllEmployers(null, null)).thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/employers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employerId").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/employers/{id} - Success returns 200 OK")
    void testDeleteEmployer_Success() throws Exception {
        doNothing().when(employerService).deleteEmployer(1L);

        mockMvc.perform(delete("/api/employers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employer with id 1 has been successfully deleted"));
    }
}
