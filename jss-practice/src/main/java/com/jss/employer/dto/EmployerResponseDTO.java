package com.jss.employer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response representation of an Employer profile")
public class EmployerResponseDTO {

    @Schema(description = "Unique Identifier of the employer", example = "1")
    private Long employerId;

    @Schema(description = "Name of the organization", example = "Google India Pvt Ltd")
    private String organizationName;

    @Schema(description = "Primary contact person", example = "Sundar Pichai")
    private String contactPerson;

    @Schema(description = "Street address", example = "RMZ Infinity, Old Madras Road")
    private String address;

    @Schema(description = "City", example = "Bengaluru")
    private String city;

    @Schema(description = "State", example = "Karnataka")
    private String state;

    @Schema(description = "Country", example = "India")
    private String country;

    @Schema(description = "Contact phone number", example = "9876543210")
    private String contactNumber;

    @Schema(description = "Organization email", example = "careers@google.com")
    private String email;

    @Schema(description = "Website URL", example = "https://careers.google.com")
    private String website;

    @Schema(description = "Industry domain", example = "Information Technology")
    private String industryType;

    @Schema(description = "Company size range", example = "10000+ Employees")
    private String companySize;

    @Schema(description = "Registration date and time", example = "2026-11-16T10:00:00")
    private LocalDateTime createdDate;

    public EmployerResponseDTO() {
    }

    public EmployerResponseDTO(Long employerId, String organizationName, String contactPerson, 
                               String address, String city, String state, String country, 
                               String contactNumber, String email, String website, 
                               String industryType, String companySize, LocalDateTime createdDate) {
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
