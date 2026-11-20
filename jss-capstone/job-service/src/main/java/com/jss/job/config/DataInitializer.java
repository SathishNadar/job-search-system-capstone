package com.jss.job.config;

import com.jss.job.entity.Job;
import com.jss.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds initial realistic job openings into MySQL ('job_db.jobs')
 * on first startup, enabling immediate 1-click testing in Swagger UI and inspection in MySQL Workbench.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JobRepository jobRepository;

    @Override
    public void run(String... args) {
        if (jobRepository.count() == 0) {
            Job job1 = Job.builder()
                    .employerId(1L)
                    .jobTitle("Senior Java Developer")
                    .designation("Lead Software Engineer")
                    .description("Design, build, and maintain high-performance enterprise microservices in a distributed banking and retail environment using Java 17 and Spring Boot.")
                    .requiredSkills("Java, Spring Boot, Microservices, MySQL, REST APIs, Docker")
                    .experienceRequired(3.0)
                    .qualification("B.Tech / B.E. in Computer Science or equivalent")
                    .salaryMin(900000.0)
                    .salaryMax(1600000.0)
                    .location("Mumbai")
                    .noticePeriod(30)
                    .contactEmail("careers.india@capgemini.com")
                    .employmentType("FULL_TIME")
                    .status("ACTIVE")
                    .createdDate(LocalDateTime.now())
                    .build();

            Job job2 = Job.builder()
                    .employerId(2L)
                    .jobTitle("Frontend Engineer (React)")
                    .designation("Senior Frontend Developer")
                    .description("Craft intuitive, responsive user experiences using React, TypeScript, and modern state management. Collaborate directly with product designers.")
                    .requiredSkills("React, TypeScript, JavaScript, Redux, HTML5, CSS3, REST APIs")
                    .experienceRequired(2.0)
                    .qualification("B.Tech / B.E. / BCA / MCA")
                    .salaryMin(1200000.0)
                    .salaryMax(2200000.0)
                    .location("Bengaluru")
                    .noticePeriod(15)
                    .contactEmail("careers@google.com")
                    .employmentType("FULL_TIME")
                    .status("ACTIVE")
                    .createdDate(LocalDateTime.now())
                    .build();

            Job job3 = Job.builder()
                    .employerId(1L)
                    .jobTitle("Cloud & DevOps Engineer")
                    .designation("DevOps Consultant")
                    .description("Automate multi-region CI/CD pipelines, containerize backend microservices, and maintain Kubernetes clusters on AWS cloud infrastructure.")
                    .requiredSkills("DevOps, Docker, Kubernetes, AWS, Terraform, CI/CD, Linux")
                    .experienceRequired(4.0)
                    .qualification("B.Tech / B.E. in Information Technology")
                    .salaryMin(1000000.0)
                    .salaryMax(1800000.0)
                    .location("Pune")
                    .noticePeriod(30)
                    .contactEmail("careers.india@capgemini.com")
                    .employmentType("FULL_TIME")
                    .status("ACTIVE")
                    .createdDate(LocalDateTime.now())
                    .build();

            jobRepository.saveAll(List.of(job1, job2, job3));
        }
    }
}
