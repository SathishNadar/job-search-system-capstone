package com.jss.employer.service;

import com.jss.employer.dto.EmployerRequestDTO;
import com.jss.employer.dto.EmployerResponseDTO;

import java.util.List;

public interface EmployerService {

    EmployerResponseDTO registerEmployer(EmployerRequestDTO requestDTO);

    EmployerResponseDTO getEmployerById(Long employerId);

    List<EmployerResponseDTO> getAllEmployers(String industryType, String city);

    EmployerResponseDTO updateEmployer(Long employerId, EmployerRequestDTO requestDTO);

    void deleteEmployer(Long employerId);
}
