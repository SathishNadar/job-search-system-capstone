package com.jss.employer.service;

import com.jss.employer.dto.EmployerRequestDTO;
import com.jss.employer.dto.EmployerResponseDTO;
import com.jss.employer.entity.Employer;
import com.jss.employer.exception.DuplicateEmployerException;
import com.jss.employer.exception.EmployerNotFoundException;
import com.jss.employer.repository.EmployerRepository;
import com.jss.employer.service.impl.EmployerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployerServiceTest {

    @Mock
    private EmployerRepository employerRepository;

    @InjectMocks
    private EmployerServiceImpl employerService;

    private Employer sampleEmployer;
    private EmployerRequestDTO sampleRequestDTO;

    @BeforeEach
    void setUp() {
        sampleEmployer = new Employer(
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

        sampleRequestDTO = new EmployerRequestDTO(
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
    @DisplayName("Positive Test: Register Employer successfully")
    void testRegisterEmployer_Success() {
        when(employerRepository.existsByEmail(sampleRequestDTO.getEmail())).thenReturn(false);
        when(employerRepository.save(any(Employer.class))).thenReturn(sampleEmployer);

        EmployerResponseDTO response = employerService.registerEmployer(sampleRequestDTO);

        assertNotNull(response);
        assertEquals(sampleEmployer.getEmployerId(), response.getEmployerId());
        assertEquals("Google India Pvt Ltd", response.getOrganizationName());
        assertEquals("careers@google.com", response.getEmail());
        verify(employerRepository, times(1)).save(any(Employer.class));
    }

    @Test
    @DisplayName("Negative Test: Register Employer with duplicate email throws DuplicateEmployerException")
    void testRegisterEmployer_DuplicateEmail_ThrowsException() {
        when(employerRepository.existsByEmail(sampleRequestDTO.getEmail())).thenReturn(true);

        assertThrows(DuplicateEmployerException.class, () -> {
            employerService.registerEmployer(sampleRequestDTO);
        });

        verify(employerRepository, never()).save(any(Employer.class));
    }

    @Test
    @DisplayName("Positive Test: Get Employer by ID successfully")
    void testGetEmployerById_Success() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(sampleEmployer));

        EmployerResponseDTO response = employerService.getEmployerById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getEmployerId());
        assertEquals("Google India Pvt Ltd", response.getOrganizationName());
    }

    @Test
    @DisplayName("Negative Test: Get Employer by non-existent ID throws EmployerNotFoundException")
    void testGetEmployerById_NotFound_ThrowsException() {
        when(employerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EmployerNotFoundException.class, () -> {
            employerService.getEmployerById(999L);
        });
    }

    @Test
    @DisplayName("Positive Test: Get all employers without filter")
    void testGetAllEmployers_NoFilter() {
        when(employerRepository.findAll()).thenReturn(Arrays.asList(sampleEmployer));

        List<EmployerResponseDTO> result = employerService.getAllEmployers(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Google India Pvt Ltd", result.get(0).getOrganizationName());
    }

    @Test
    @DisplayName("Positive Test: Search employers by industry and city")
    void testGetAllEmployers_WithFilters() {
        when(employerRepository.findByIndustryTypeIgnoreCaseAndCityIgnoreCase("Information Technology", "Bengaluru"))
                .thenReturn(Arrays.asList(sampleEmployer));

        List<EmployerResponseDTO> result = employerService.getAllEmployers("Information Technology", "Bengaluru");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bengaluru", result.get(0).getCity());
    }

    @Test
    @DisplayName("Positive Test: Update Employer successfully")
    void testUpdateEmployer_Success() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(sampleEmployer));
        when(employerRepository.save(any(Employer.class))).thenReturn(sampleEmployer);

        sampleRequestDTO.setOrganizationName("Alphabet Google Inc");
        EmployerResponseDTO response = employerService.updateEmployer(1L, sampleRequestDTO);

        assertNotNull(response);
        verify(employerRepository, times(1)).save(any(Employer.class));
    }

    @Test
    @DisplayName("Positive Test: Delete Employer successfully")
    void testDeleteEmployer_Success() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(sampleEmployer));
        doNothing().when(employerRepository).delete(sampleEmployer);

        assertDoesNotThrow(() -> employerService.deleteEmployer(1L));
        verify(employerRepository, times(1)).delete(sampleEmployer);
    }
}
