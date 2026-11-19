package com.jss.employer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employers")
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

    // Constructors
    public Employer() {
    }

    public Employer(Long employerId, String organizationName, String contactPerson, String address, 
                    String city, String state, String country, String contactNumber, String email, 
                    String website, String industryType, String companySize, LocalDateTime createdDate) {
        this.employerId = employerId;
        this.organizationName = organizationName;
        this.contactPerson = contactPerson;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.contactNumber = contactNumber;
        this.email = email;
        this.website = website;
        this.industryType = industryType;
        this.companySize = companySize;
        this.createdDate = createdDate;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdDate == null) {
            this.createdDate = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getEmployerId() {
        return employerId;
    }

    public void setEmployerId(Long employerId) {
        this.employerId = employerId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getCompanySize() {
        return companySize;
    }

    public void setCompanySize(String companySize) {
        this.companySize = companySize;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
