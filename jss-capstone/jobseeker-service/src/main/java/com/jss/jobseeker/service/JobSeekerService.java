package com.jss.jobseeker.service;

import com.jss.jobseeker.dto.JobSeekerRequestDTO;
import com.jss.jobseeker.dto.JobSeekerResponseDTO;

import java.util.List;

/**
 * Service contract defining business operations for Job Seeker / Candidate profile management.
 */
public interface JobSeekerService {

    /**
     * Registers a new candidate profile after enforcing duplicate email validation.
     *
     * @param requestDTO candidate profile details
     * @return created candidate profile
     */
    JobSeekerResponseDTO registerJobSeeker(JobSeekerRequestDTO requestDTO);

    /**
     * Retrieves a candidate profile by unique ID.
     *
     * @param id candidate ID
     * @return candidate profile details
     */
    JobSeekerResponseDTO getJobSeekerById(Long id);

    /**
     * Retrieves all registered candidates (simple directory listing).
     *
     * @return list of all candidate profiles
     */
    List<JobSeekerResponseDTO> getAllJobSeekers();

    /**
     * Unified candidate search and filtering.
     * Dynamically filters candidate pool by skill (partial match), city, and/or minimum experience.
     *
     * @param skill optional skill keyword (e.g. "Java", "React")
     * @param city optional city name (e.g. "Mumbai")
     * @param minExperience optional minimum experience threshold in years (e.g. 3.0)
     * @return list of matching candidate profiles
     */
    List<JobSeekerResponseDTO> searchCandidates(String skill, String city, Double minExperience);

    /**
     * Updates an existing candidate profile with smart email conflict detection.
     *
     * @param id candidate ID to update
     * @param requestDTO updated details
     * @return updated candidate profile
     */
    JobSeekerResponseDTO updateJobSeeker(Long id, JobSeekerRequestDTO requestDTO);

    /**
     * Deletes a candidate profile by ID.
     *
     * @param id candidate ID to delete
     */
    void deleteJobSeeker(Long id);
}
