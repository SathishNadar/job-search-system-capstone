package com.jss.employer.service.impl;

import com.jss.employer.dto.EmployerRequestDTO;
import com.jss.employer.dto.EmployerResponseDTO;

import com.jss.employer.entity.Employer;

import com.jss.employer.exception.DuplicateEmployerException;
import com.jss.employer.exception.EmployerNotFoundException;

import com.jss.employer.repository.EmployerRepository;
import com.jss.employer.service.EmployerService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployerServiceImpl implements EmployerService {

    private final EmployerRepository employerRepository;

    public EmployerServiceImpl(EmployerRepository employerRepository) {
        this.employerRepository = employerRepository;
    }

    @Override
    public EmployerResponseDTO registerEmployer(EmployerRequestDTO requestDTO) {
        if (employerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateEmployerException(
                    "An employer with email '" + requestDTO.getEmail() + "' already exists");
        }

        Employer employer = mapToEntity(requestDTO);
        Employer savedEmployer = employerRepository.save(employer);
        return mapToResponseDTO(savedEmployer);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployerResponseDTO getEmployerById(Long employerId) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new EmployerNotFoundException(employerId));
        return mapToResponseDTO(employer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployerResponseDTO> getAllEmployers(String industryType, String city) {
        List<Employer> employers;

        if (industryType != null && !industryType.trim().isEmpty() && city != null && !city.trim().isEmpty()) {
            employers = employerRepository.findByIndustryTypeIgnoreCaseAndCityIgnoreCase(industryType.trim(),
                    city.trim());
        } else if (industryType != null && !industryType.trim().isEmpty()) {
            employers = employerRepository.findByIndustryTypeIgnoreCase(industryType.trim());
        } else if (city != null && !city.trim().isEmpty()) {
            employers = employerRepository.findByCityIgnoreCase(city.trim());
        } else {
            employers = employerRepository.findAll();
        }

        return employers.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployerResponseDTO updateEmployer(Long employerId, EmployerRequestDTO requestDTO) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new EmployerNotFoundException(employerId));

        // Check if email is changed and new email already belongs to another employer
        if (!employer.getEmail().equalsIgnoreCase(requestDTO.getEmail()) &&
                employerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateEmployerException(
                    "An employer with email '" + requestDTO.getEmail() + "' already exists");
        }

        employer.setOrganizationName(requestDTO.getOrganizationName());
        employer.setContactPerson(requestDTO.getContactPerson());
        employer.setAddress(requestDTO.getAddress());
        employer.setCity(requestDTO.getCity());
        employer.setState(requestDTO.getState());
        employer.setCountry(requestDTO.getCountry());
        employer.setContactNumber(requestDTO.getContactNumber());
        employer.setEmail(requestDTO.getEmail());
        employer.setWebsite(requestDTO.getWebsite());
        employer.setIndustryType(requestDTO.getIndustryType());
        employer.setCompanySize(requestDTO.getCompanySize());

        Employer updatedEmployer = employerRepository.save(employer);
        return mapToResponseDTO(updatedEmployer);
    }

    @Override
    public void deleteEmployer(Long employerId) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new EmployerNotFoundException(employerId));
        employerRepository.delete(employer);
    }

    // Helper mapping methods
    private Employer mapToEntity(EmployerRequestDTO dto) {
        Employer employer = new Employer();
        employer.setOrganizationName(dto.getOrganizationName());
        employer.setContactPerson(dto.getContactPerson());
        employer.setAddress(dto.getAddress());
        employer.setCity(dto.getCity());
        employer.setState(dto.getState());
        employer.setCountry(dto.getCountry());
        employer.setContactNumber(dto.getContactNumber());
        employer.setEmail(dto.getEmail());
        employer.setWebsite(dto.getWebsite());
        employer.setIndustryType(dto.getIndustryType());
        employer.setCompanySize(dto.getCompanySize());
        return employer;
    }

    private EmployerResponseDTO mapToResponseDTO(Employer employer) {
        return new EmployerResponseDTO(
                employer.getEmployerId(),
                employer.getOrganizationName(),
                employer.getContactPerson(),
                employer.getAddress(),
                employer.getCity(),
                employer.getState(),
                employer.getCountry(),
                employer.getContactNumber(),
                employer.getEmail(),
                employer.getWebsite(),
                employer.getIndustryType(),
                employer.getCompanySize(),
                employer.getCreatedDate());
    }
}
