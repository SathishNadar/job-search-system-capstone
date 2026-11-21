package com.jss.application.repository;

import com.jss.application.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    List<JobApplication> findByJobSeekerId(Long jobSeekerId);

    List<JobApplication> findByJobId(Long jobId);

    List<JobApplication> findByJobIdAndApplicationStatusIgnoreCase(Long jobId, String applicationStatus);
}
