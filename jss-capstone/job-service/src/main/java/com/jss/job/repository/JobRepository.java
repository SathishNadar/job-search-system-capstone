package com.jss.job.repository;

import com.jss.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Job entity operations.
 * Communicates with local MySQL database ('job_db').
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByEmployerIdAndJobTitleIgnoreCaseAndStatus(Long employerId, String jobTitle, String status);

    /**
     * Retrieves all jobs published by a specific employer (both ACTIVE and INACTIVE).
     */
    List<Job> findByEmployerId(Long employerId);

    /**
     * Retrieves all jobs with a specific status (e.g. 'ACTIVE').
     */
    List<Job> findByStatusIgnoreCase(String status);

    /**
     * Unified 5-factor candidate job search and filter query.
     * Searches only ACTIVE job postings and dynamically filters by:
     * skill, jobTitle, designation, location, and/or employerId.
     * Any parameter left null is ignored in the query.
     */
    @Query("SELECT j FROM Job j WHERE " +
            "LOWER(j.status) = 'active' AND " +
            "(:skill IS NULL OR LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :skill, '%'))) AND " +
            "(:jobTitle IS NULL OR LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
            "(:designation IS NULL OR LOWER(j.designation) LIKE LOWER(CONCAT('%', :designation, '%'))) AND " +
            "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:employerId IS NULL OR j.employerId = :employerId)")
    List<Job> searchJobs(
            @Param("skill") String skill,
            @Param("jobTitle") String jobTitle,
            @Param("designation") String designation,
            @Param("location") String location,
            @Param("employerId") Long employerId);
}
