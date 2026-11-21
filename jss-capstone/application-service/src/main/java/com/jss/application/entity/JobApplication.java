package com.jss.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "job_seeker_id", nullable = false)
    private Long jobSeekerId;

    @Column(name = "application_date", nullable = false, updatable = false)
    private LocalDateTime applicationDate;

    @Column(name = "application_status", nullable = false, length = 30)
    private String applicationStatus;

    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    @Column(name = "employer_remarks", length = 1000)
    private String employerRemarks;

    @Column(name = "status_updated_date")
    private LocalDateTime statusUpdatedDate;

    @PrePersist
    protected void onCreate() {
        if (this.applicationDate == null) {
            this.applicationDate = LocalDateTime.now();
        }
        if (this.applicationStatus == null || this.applicationStatus.isBlank()) {
            this.applicationStatus = "APPLIED";
        }
    }
}
