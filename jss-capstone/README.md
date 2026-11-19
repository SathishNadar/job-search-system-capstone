# Job Search System (JSS) — Capstone Project (`jss-capstone`)

Welcome to the **Job Search System (JSS)** enterprise microservices project.

---

## 1. Architecture Overview & Microservice Fleet

This project is organized as a **Maven Multi-Module parent project**. It consists of **8 focused microservices**:

```
jss-capstone/                           <-- Parent Multi-Module Root (pom.xml)
├── eureka-server/                      <-- Step 1: Netflix Eureka Registry (Port 8761)
├── employer-service/                   <-- Step 2A: Employer Profiles (Port 8082)
├── jobseeker-service/                  <-- Step 2B: Candidate Profiles & Skills (Port 8083)
├── job-service/                        <-- Step 3: Job Postings & Matching (Port 8084)
├── application-service/                <-- Step 4: Applications & Shortlisting (Port 8085)
├── messaging-service/                  <-- Step 5: Direct Employer-Candidate Messaging (Port 8087)
├── auth-service/                       <-- Step 6: JWT Authentication & Roles (Port 8081)
└── api-gateway/                        <-- Step 7: Spring Cloud API Gateway (Port 8080)
```

---

## 2. Port Allocation Matrix

| Module | Service Name | Default Port | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| `eureka-server` | Eureka Discovery Server | `8761` | Dynamic service registration & discovery. |
| `api-gateway` | Spring Cloud Gateway | `8080` | Single entry point, routing, rate limiting, JWT check. |
| `auth-service` | Authentication Service | `8081` | User registration, login, JWT token generation. |
| `employer-service` | Employer Service | `8082` | Company profiles and industry/city search. |
| `jobseeker-service`| Job Seeker Service | `8083` | Candidate profiles, resumes, skills. |
| `job-service` | Job Service | `8084` | Job postings, requirements, and salaries. |
| `application-service`| Job Application Service | `8085`| Application lifecycle & hiring status. |
| `messaging-service`| Messaging Service | `8087` | In-app messaging for shortlisted candidates. |

---

## 3. Technology Stack & Key Standards

* **Framework:** Spring Boot 3.3.5 & Spring Cloud 2023.0.3
* **Language:** Java 17 / 21+ compatible
* **Inter-Service Communication:** Spring Cloud OpenFeign
* **Boilerplate Reduction:** Project Lombok (All Entities & DTOs)
* **API Documentation:** Springdoc OpenAPI 3 (Swagger UI on each service)
* **Persistence:** Spring Data JPA + Hibernate (H2 for zero-config local dev, PostgreSQL ready)

---

## 4. Build Commands

* **Compile and build all modules:**
  ```powershell
  .\mvnw.cmd clean package -DskipTests
  ```
* **Clean output directories:**
  ```powershell
  .\mvnw.cmd clean
  ```
