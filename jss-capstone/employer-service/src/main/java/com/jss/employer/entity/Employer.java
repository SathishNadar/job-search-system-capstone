package com.jss.employer.entity;

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
@Table(name = "employers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employerId;

    @Column(nullable = false)
    private String organizationName;

    @Column(nullable = false)
    private String contactPerson;

    private String address;

    @Column(nullable = false)
    private String city;

    private String state;

    private String country;

    @Column(nullable = false)
    private String contactNumber;

    @Column(nullable = false, unique = true)
    private String email;

    private String website;

    @Column(nullable = false)
    private String industryType;

    private String companySize;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // What it does: @PrePersist is a JPA Lifecycle Callback. It tells JPA: "Right
    // before you execute the SQL INSERT statement for this entity, run this method
    // first!"
    // Pairing with @Column(updatable = false): Guarantees that once created, the
    // createdDate can never be overwritten by a later update.
    @PrePersist
    protected void onCreate() {
        if (this.createdDate == null) {
            this.createdDate = LocalDateTime.now();
        }
    }
}
