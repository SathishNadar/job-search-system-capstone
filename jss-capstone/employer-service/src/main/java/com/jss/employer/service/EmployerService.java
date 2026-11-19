package com.jss.employer.service;

import com.jss.employer.dto.EmployerRequestDTO;
import com.jss.employer.dto.EmployerResponseDTO;

import java.util.List;

/**
 * Service contract defining business operations for Employer management.
 */
public interface EmployerService {

    EmployerResponseDTO registerEmployer(EmployerRequestDTO requestDTO);

    EmployerResponseDTO getEmployerById(Long id);

    List<EmployerResponseDTO> getAllEmployers(String industryType, String city);

    List<EmployerResponseDTO> searchByOrganizationName(String name);

    EmployerResponseDTO updateEmployer(Long id, EmployerRequestDTO requestDTO);

    void deleteEmployer(Long id);
}
