package com.jss.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Column(nullable = false)
    private Long employerId;

    @Column(nullable = false)
    private String jobTitle;

    private String designation;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 1000)
    private String requiredSkills;

    @Column(nullable = false)
    private Double experienceRequired;

    private String qualification;

    @Column(nullable = false)
    private Double salaryMin;

    @Column(nullable = false)
    private Double salaryMax;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer noticePeriod;

    @Column(nullable = false)
    private String contactEmail;

    @Column(nullable = false)
    private String employmentType;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        if (this.createdDate == null) {
            this.createdDate = LocalDateTime.now();
        }
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "ACTIVE";
        }
    }
}
