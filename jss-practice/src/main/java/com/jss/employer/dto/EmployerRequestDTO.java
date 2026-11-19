package com.jss.employer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body payload for registering or updating an employer profile")
public class EmployerRequestDTO {

    @NotBlank(message = "Organization name cannot be blank")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    @Schema(description = "Name of the hiring organization", example = "Google India Pvt Ltd")
    private String organizationName;

    @NotBlank(message = "Contact person name cannot be blank")
    @Size(min = 2, max = 50, message = "Contact person name must be between 2 and 50 characters")
    @Schema(description = "Primary contact representative", example = "Sundar Pichai")
    private String contactPerson;

    @Schema(description = "Street address of the organization", example = "RMZ Infinity, Old Madras Road")
    private String address;

    @NotBlank(message = "City cannot be blank")
    @Schema(description = "City where the office is located", example = "Bengaluru")
    private String city;

    @Schema(description = "State where the office is located", example = "Karnataka")
    private String state;

    @Schema(description = "Country", example = "India")
    private String country;

    @NotBlank(message = "Contact number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be a valid 10-digit phone number")
    @Schema(description = "10-digit mobile or telephone number", example = "9876543210")
    private String contactNumber;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email format")
    @Schema(description = "Official organization email address", example = "careers@google.com")
    private String email;

    @Schema(description = "Official website URL", example = "https://careers.google.com")
    private String website;

    @NotBlank(message = "Industry type cannot be blank")
    @Schema(description = "Primary industry domain", example = "Information Technology")
    private String industryType;

    @Schema(description = "Company size range", example = "10000+ Employees")
    private String companySize;

    public EmployerRequestDTO() {
    }

    public EmployerRequestDTO(String organizationName, String contactPerson, String address, 
                              String city, String state, String country, String contactNumber, 
                              String email, String website, String industryType, String companySize) {
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
    }

    // Getters and Setters
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
}
