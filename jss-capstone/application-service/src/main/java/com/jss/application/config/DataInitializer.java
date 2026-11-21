package com.jss.application.config;

import com.jss.application.entity.JobApplication;
import com.jss.application.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds sample application records if MySQL table is empty on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final JobApplicationRepository applicationRepository;

    @Override
    public void run(String... args) {
        if (applicationRepository.count() == 0) {
            log.info("Seeding initial Job Application records into MySQL ('application_db')...");

            JobApplication app1 = JobApplication.builder()
                    .jobId(1L)
                    .jobSeekerId(1L)
                    .applicationDate(LocalDateTime.now().minusDays(3))
                    .applicationStatus("APPLIED")
                    .resumeUrl("https://storage.jss.com/resumes/sathish_nadar_resume.pdf")
                    .build();

            JobApplication app2 = JobApplication.builder()
                    .jobId(1L)
                    .jobSeekerId(2L)
                    .applicationDate(LocalDateTime.now().minusDays(5))
                    .applicationStatus("SHORTLISTED")
                    .statusUpdatedDate(LocalDateTime.now().minusDays(1))
                    .resumeUrl("https://storage.jss.com/resumes/priya_sharma_resume.pdf")
                    .employerRemarks("Strong Spring Boot and Cloud experience, invited for interview")
                    .build();

            applicationRepository.save(app1);
            applicationRepository.save(app2);

            log.info("Successfully seeded 2 Job Application records.");
        } else {
            log.info("Job applications table already contains data. Skipping seeding.");
        }
    }
}
