package com.jss.employer.repository;

import com.jss.employer.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Employer entity operations.
 * Communicates with local MySQL database (employer_db).
 */
@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {

    boolean existsByEmail(String email);

    Optional<Employer> findByEmail(String email);

    List<Employer> findByCityIgnoreCase(String city);

    List<Employer> findByIndustryTypeIgnoreCase(String industryType);

    List<Employer> findByIndustryTypeIgnoreCaseAndCityIgnoreCase(String industryType, String city);

    List<Employer> findByOrganizationNameContainingIgnoreCase(String organizationName);
}
