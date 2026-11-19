package com.jss.jobseeker.config;

import com.jss.jobseeker.entity.JobSeeker;
import com.jss.jobseeker.repository.JobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JobSeekerRepository jobSeekerRepository;

    @Override
    public void run(String... args) {
        if (jobSeekerRepository.count() == 0) {
            JobSeeker candidate1 = JobSeeker.builder()
                    .firstName("Sathish")
                    .lastName("Nadar")
                    .email("sathish.nadar@example.com")
                    .contactNumber("9876543210")
                    .address("Flat 402, Sunshine Heights, Powai")
                    .city("Mumbai")
                    .state("Maharashtra")
                    .totalExperience(3.5)
                    .currentDesignation("Software Engineer")
                    .expectedCTC(1200000.0)
                    .noticePeriod(30)
                    .skills("Java, Spring Boot, Microservices, MySQL, REST APIs")
                    .qualification("B.Tech in Computer Science")
                    .createdDate(LocalDateTime.now())
                    .build();

            JobSeeker candidate2 = JobSeeker.builder()
                    .firstName("Priya")
                    .lastName("Sharma")
                    .email("priya.sharma@example.com")
                    .contactNumber("9123456789")
                    .address("Indiranagar, 12th Main Road")
                    .city("Bengaluru")
                    .state("Karnataka")
                    .totalExperience(2.0)
                    .currentDesignation("Frontend Developer")
                    .expectedCTC(850000.0)
                    .noticePeriod(15)
                    .skills("React, JavaScript, TypeScript, Redux, HTML5, CSS3")
                    .qualification("B.E. in Information Technology")
                    .createdDate(LocalDateTime.now())
                    .build();

            JobSeeker candidate3 = JobSeeker.builder()
                    .firstName("Rahul")
                    .lastName("Verma")
                    .email("rahul.verma@example.com")
                    .contactNumber("9988776655")
                    .address("Koregaon Park, Lane 7")
                    .city("Pune")
                    .state("Maharashtra")
                    .totalExperience(5.0)
                    .currentDesignation("DevOps Engineer")
                    .expectedCTC(1800000.0)
                    .noticePeriod(0)
                    .skills("DevOps, Docker, Kubernetes, AWS, Terraform, CI/CD")
                    .qualification("M.Tech in Software Engineering")
                    .createdDate(LocalDateTime.now())
                    .build();

            jobSeekerRepository.saveAll(List.of(candidate1, candidate2, candidate3));
        }
    }
}
