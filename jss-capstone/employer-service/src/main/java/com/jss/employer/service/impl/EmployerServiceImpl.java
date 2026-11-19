package com.jss.employer.service.impl;

import com.jss.employer.dto.EmployerRequestDTO;
import com.jss.employer.dto.EmployerResponseDTO;
import com.jss.employer.entity.Employer;
import com.jss.employer.exception.DuplicateEmployerException;
import com.jss.employer.exception.EmployerNotFoundException;
import com.jss.employer.repository.EmployerRepository;
import com.jss.employer.service.EmployerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EmployerService business logic.
 * Handles validation, persistence into MySQL, and DTO-Entity conversions using Lombok builders.
 */
@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements EmployerService {

    private final EmployerRepository employerRepository;

    @Override
    @Transactional
    public EmployerResponseDTO registerEmployer(EmployerRequestDTO requestDTO) {
        if (employerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateEmployerException("Employer already registered with email: " + requestDTO.getEmail());
        }

        Employer employer = mapToEntity(requestDTO);
        Employer savedEmployer = employerRepository.save(employer);

        return mapToResponseDTO(savedEmployer);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployerResponseDTO getEmployerById(Long id) {
        Employer employer = employerRepository.findById(id)
                                              .orElseThrow(() -> new EmployerNotFoundException(id));
        return mapToResponseDTO(employer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployerResponseDTO> getAllEmployers(String industryType, String city) {

        List<Employer> employers;
        boolean hasIndustry = (industryType != null && !industryType.trim().isEmpty());
        boolean hasCity = (city != null && !city.trim().isEmpty());

        if (hasIndustry && hasCity) {
            employers = employerRepository.findByIndustryTypeIgnoreCaseAndCityIgnoreCase(industryType.trim(), city.trim());
        } else if (hasIndustry) {
            employers = employerRepository.findByIndustryTypeIgnoreCase(industryType.trim());
        } else if (hasCity) {
            employers = employerRepository.findByCityIgnoreCase(city.trim());
        } else {
            employers = employerRepository.findAll();
        }

        return employers.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployerResponseDTO> searchByOrganizationName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return getAllEmployers(null, null);
        }

        return employerRepository.findByOrganizationNameContainingIgnoreCase(name.trim())
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployerResponseDTO updateEmployer(Long id, EmployerRequestDTO requestDTO) {

        Employer existingEmployer = employerRepository.findById(id)
                                                      .orElseThrow(() -> new EmployerNotFoundException(id));

        // Smart duplicate check: only validate if email is actually being changed
        if (!existingEmployer.getEmail().equalsIgnoreCase(requestDTO.getEmail())) {
            employerRepository.findByEmail(requestDTO.getEmail()).ifPresent(found -> {
                if (!found.getEmployerId().equals(id)) {
                    throw new DuplicateEmployerException(requestDTO.getEmail(), found.getEmployerId());
                }
            });
        }

        existingEmployer.setOrganizationName(requestDTO.getOrganizationName());
        existingEmployer.setContactPerson(requestDTO.getContactPerson());
        existingEmployer.setAddress(requestDTO.getAddress());
        existingEmployer.setCity(requestDTO.getCity());
        existingEmployer.setState(requestDTO.getState());
        existingEmployer.setCountry(requestDTO.getCountry());
        existingEmployer.setContactNumber(requestDTO.getContactNumber());
        existingEmployer.setEmail(requestDTO.getEmail());
        existingEmployer.setWebsite(requestDTO.getWebsite());
        existingEmployer.setIndustryType(requestDTO.getIndustryType());
        existingEmployer.setCompanySize(requestDTO.getCompanySize());

        Employer updatedEmployer = employerRepository.save(existingEmployer);

        return mapToResponseDTO(updatedEmployer);
    }

    @Override
    @Transactional
    public void deleteEmployer(Long id) {

        Employer existingEmployer = employerRepository.findById(id)
                                                      .orElseThrow(() -> new EmployerNotFoundException(id));

        /*
         * FUTURE EXTENSION POINT (Step 3):
         * When 'job-service' and 'JobServiceClient' (OpenFeign) are integrated:
         *
         * long activeJobsCount = jobServiceClient.countActiveJobsByEmployerId(id);
         * if (activeJobsCount > 0) {
         *     throw new CannotDeleteEmployerWithActiveJobsException(id, activeJobsCount);
         * }
         */

        employerRepository.delete(existingEmployer);
    }

    // Helper mapping methods using Lombok @Builder
    private Employer mapToEntity(EmployerRequestDTO dto) {
        return Employer.builder()
                .organizationName(dto.getOrganizationName())
                .contactPerson(dto.getContactPerson())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .contactNumber(dto.getContactNumber())
                .email(dto.getEmail())
                .website(dto.getWebsite())
                .industryType(dto.getIndustryType())
                .companySize(dto.getCompanySize())
                .build();
    }

    private EmployerResponseDTO mapToResponseDTO(Employer entity) {
        return EmployerResponseDTO.builder()
                .employerId(entity.getEmployerId())
                .organizationName(entity.getOrganizationName())
                .contactPerson(entity.getContactPerson())
                .address(entity.getAddress())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .contactNumber(entity.getContactNumber())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .industryType(entity.getIndustryType())
                .companySize(entity.getCompanySize())
                .createdDate(entity.getCreatedDate())
                .build();
    }
}
