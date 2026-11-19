package com.jss.jobseeker.repository;

import com.jss.jobseeker.entity.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for JobSeeker entity operations.
 * Communicates with the local MySQL database ('jobseeker_db').
 */
@Repository
public interface JobSeekerRepository extends JpaRepository<JobSeeker, Long> {

        boolean existsByEmail(String email);

        Optional<JobSeeker> findByEmail(String email);

        List<JobSeeker> findBySkillsContainingIgnoreCase(String skill);

        List<JobSeeker> findByCityIgnoreCase(String city);

        List<JobSeeker> findByTotalExperienceGreaterThanEqual(Double experience);

        @Query("SELECT j FROM JobSeeker j WHERE " +
                        "(:skill IS NULL OR LOWER(j.skills) LIKE LOWER(CONCAT('%', :skill, '%'))) AND " +
                        "(:city IS NULL OR LOWER(j.city) = LOWER(:city)) AND " +
                        "(:minExperience IS NULL OR j.totalExperience >= :minExperience)")
        List<JobSeeker> searchCandidates(
                        @Param("skill") String skill,
                        @Param("city") String city,
                        @Param("minExperience") Double minExperience);
}
