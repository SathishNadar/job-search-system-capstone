package com.jss.employer.config;

import com.jss.employer.entity.Employer;
import com.jss.employer.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds initial realistic employer records into the MySQL database ('employer_db')
 * when the application boots up for the first time, enabling immediate testing in Swagger UI
 * and direct inspection in MySQL Workbench.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EmployerRepository employerRepository;

    @Override
    public void run(String... args) {
        if (employerRepository.count() == 0) {
            log.info("No existing employers found in MySQL. Seeding initial realistic employer data...");

            Employer capgemini = Employer.builder()
                    .organizationName("Capgemini Technology Services")
                    .contactPerson("Sathish Nadar")
                    .address("Plant 2, Godrej & Boyce Complex, Vikhroli")
                    .city("Mumbai")
                    .state("Maharashtra")
                    .country("India")
                    .contactNumber("9876543210")
                    .email("careers.india@capgemini.com")
                    .website("https://www.capgemini.com")
                    .industryType("Information Technology")
                    .companySize("10000+ Employees")
                    .createdDate(LocalDateTime.now())
                    .build();

            Employer google = Employer.builder()
                    .organizationName("Google India Pvt Ltd")
                    .contactPerson("Sundar Pichai")
                    .address("RMZ Infinity, Tower E, Old Madras Road")
                    .city("Bengaluru")
                    .state("Karnataka")
                    .country("India")
                    .contactNumber("9123456780")
                    .email("careers@google.com")
                    .website("https://careers.google.com")
                    .industryType("Information Technology")
                    .companySize("10000+ Employees")
                    .createdDate(LocalDateTime.now())
                    .build();

            Employer hdfc = Employer.builder()
                    .organizationName("HDFC Bank Ltd")
                    .contactPerson("Sashidhar Jagdishan")
                    .address("HDFC Bank House, Senapati Bapat Marg, Lower Parel")
                    .city("Mumbai")
                    .state("Maharashtra")
                    .country("India")
                    .contactNumber("9988776655")
                    .email("recruitment@hdfcbank.com")
                    .website("https://www.hdfcbank.com")
                    .industryType("Banking & Financial Services")
                    .companySize("10000+ Employees")
                    .createdDate(LocalDateTime.now())
                    .build();

            employerRepository.saveAll(List.of(capgemini, google, hdfc));
            log.info("Seeded 3 sample employers into MySQL 'employer_db.employers'.");
        } else {
            log.info("MySQL 'employer_db.employers' already contains {} records. Skipping data initialization.",
                    employerRepository.count());
        }
    }
}
