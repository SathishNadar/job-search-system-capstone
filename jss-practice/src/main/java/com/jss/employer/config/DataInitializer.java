package com.jss.employer.config;

import com.jss.employer.entity.Employer;
import com.jss.employer.repository.EmployerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployerRepository employerRepository;

    public DataInitializer(EmployerRepository employerRepository) {
        this.employerRepository = employerRepository;
    }

    @Override
    public void run(String... args) {
        if (employerRepository.count() == 0) {
            Employer employer1 = new Employer(
                    null,
                    "Capgemini Technology Services",
                    "Rajesh Kumar",
                    "Whitefield, ITPL Main Road",
                    "Bengaluru",
                    "Karnataka",
                    "India",
                    "9876543210",
                    "recruitment@capgemini.com",
                    "https://www.capgemini.com",
                    "Information Technology",
                    "10000+ Employees",
                    LocalDateTime.now()
            );

            Employer employer2 = new Employer(
                    null,
                    "Google India Pvt Ltd",
                    "Priya Sharma",
                    "RMZ Infinity, Old Madras Road",
                    "Bengaluru",
                    "Karnataka",
                    "India",
                    "9123456780",
                    "careers@google.com",
                    "https://careers.google.com",
                    "Information Technology",
                    "10000+ Employees",
                    LocalDateTime.now()
            );

            Employer employer3 = new Employer(
                    null,
                    "HDFC Bank Corporate",
                    "Amit Patel",
                    "Senapati Bapat Marg, Lower Parel",
                    "Mumbai",
                    "Maharashtra",
                    "India",
                    "9988776655",
                    "hr@hdfcbank.com",
                    "https://www.hdfcbank.com",
                    "Banking & Financial Services",
                    "5000-10000 Employees",
                    LocalDateTime.now()
            );

            employerRepository.saveAll(Arrays.asList(employer1, employer2, employer3));
            System.out.println(">>> Sample Employer Data Initialized successfully! <<<");
        }
    }
}
