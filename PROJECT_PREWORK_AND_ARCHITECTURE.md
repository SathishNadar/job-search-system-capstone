# Job Search System (JSS) — Architecture Design, Evolution & Prework Blueprint

> **Project:** Job Search System (JSS) Using Microservices Architecture  
> **Author / Engineer:** Development Team  
> **Document Purpose:** A formal architectural record detailing the transition from the raw case study specifications to an optimized, enterprise-grade engineering plan. This document explains the design choices, architectural trade-offs, scope rationalization, and step-by-step phased build sequence.

---

## Table of Contents
1. [Initial Requirements Analysis (What the Case Study Document Specified)](#1-initial-requirements-analysis-what-the-case-study-document-specified)
2. [Strategic Architectural Decisions & Scope Refinement](#2-strategic-architectural-decisions--scope-refinement)
   - [Decision 1: Scope Rationalization (Eliminating the Basket Service)](#decision-1-scope-rationalization-eliminating-the-basket-service)
   - [Decision 2: Project Structure (Maven Multi-Module Parent vs. Independent Repos)](#decision-2-project-structure-maven-multi-module-parent-vs-independent-repos)
   - [Decision 3: Inter-Service Communication Strategy (OpenFeign vs. RabbitMQ)](#decision-3-inter-service-communication-strategy-openfeign-vs-rabbitmq)
   - [Decision 4: Testing & Execution Strategy (Backend Momentum First)](#decision-4-testing--execution-strategy-backend-momentum-first)
3. [The Optimized 8-Microservice Ecosystem & Port Allocation Matrix](#3-the-optimized-8-microservice-ecosystem--port-allocation-matrix)
4. [The 8-Step Phased Build Sequence (From the Ground Up with Engineering Rationale)](#4-the-8-step-phased-build-sequence-from-the-ground-up-with-engineering-rationale)
5. [Summary & Next Execution Steps](#5-summary--next-execution-steps)

---

# 1. Initial Requirements Analysis (What the Case Study Document Specified)

### 1.1 The Original Problem Statement
The **Job Search System (JSS)** is designed as an end-to-end recruitment platform connecting **Employers** (who post jobs, search candidates, and manage applications) and **Job Seekers** (who create profiles, upload resumes, search jobs, and apply).

### 1.2 The Raw 9-Microservice Proposal in the Document
The initial document proposed breaking the application into 9 distinct services:
1. **API Gateway Service**: Routing, security, rate limiting.
2. **Eureka Discovery Service**: Central registry and dynamic discovery.
3. **Authentication Service**: User registration, login, JWT token generation.
4. **Employer Service**: Employer profiles and search.
5. **Job Seeker Service**: Resume profiles, skills, candidate details.
6. **Job Service**: Job postings, criteria, salaries.
7. **Job Application Service**: Application processing and status updates.
8. **Favorites / Basket Service**: Storing saved/bookmarked jobs.
9. **Messaging Service**: Employer-candidate messaging.

### 1.3 The Choices & Dual Options Given in the Document
The case study specification gave several technology options without prescribing the exact implementation path:
* **Inter-Service Communication**:
  * *Option 1:* Synchronous REST via **Spring Cloud OpenFeign** / **WebClient**.
  * *Option 2:* Event-Driven Asynchronous via **Apache Kafka** or **RabbitMQ**.
* **Language & Framework**:
  * Mentioned Java 8, while modern industry standards and active Spring Cloud dependencies require **Java 17 / 21+** with **Spring Boot 3.x**.
* **Testing & Documentation**:
  * Mandatory **Swagger / OpenAPI 3** documentation.
  * Mandatory **Postman Collection**.
  * Unit & integration tests via **JUnit 5 & Mockito**.

---

# 2. Strategic Architectural Decisions & Scope Refinement

Rather than blindly following the document, we critically evaluated the requirements from a real-world software engineering perspective.

---

### Decision 1: Scope Rationalization (Eliminating the Basket Service)

* **The Guide's Expectation**: The project guide and evaluation rubric expected approximately **7 microservices**.
* **The Raw Document**: Proposed 9 microservices.
* **Our Evaluation**:
  * The **Favorites / Basket Service** (`JobBasket`: `basketId`, `jobSeekerId`, `jobId`, `addedDate`) is merely an auxiliary bookmark table.
  * It adds **zero architectural value** to the core recruitment workflow (Job Posting $\rightarrow$ Application $\rightarrow$ Review $\rightarrow$ Shortlist $\rightarrow$ Communication).
  * Implementing it as a standalone microservice introduces unnecessary overhead, network hops, and operational complexity for a basic bookmark list.
* **The Decision**:
  * **Eliminate the Basket Service**.
  * This refines our architecture to a clean, highly focused **8-microservice ecosystem**.
  * **Significance:** This demonstrates active engineering decision-making and requirements grooming rather than passive, blind compliance.

---

### Decision 2: Project Structure (Maven Multi-Module Parent vs. Independent Repos)

We evaluated two architectural structures for managing the project repository:

| Feature / Criteria | Approach A: Independent Repositories | Approach B: Maven Multi-Module Parent Project (CHOSEN) |
| :--- | :--- | :--- |
| **Folder Organization** | 8 completely separate folders with 8 independent `pom.xml` files. | 1 Root folder (`job-search-system`) containing all 8 services as sub-modules. |
| **IDE Experience** | Must open 8 separate IDE windows, consuming massive CPU and RAM. | **Open ONE single IDE window**. All services, configurations, and shared models are visible together. |
| **Dependency Management** | Repeating Spring Boot, Spring Cloud, Lombok, and Swagger versions across 8 separate `pom.xml` files. High risk of version mismatch. | **Centralized `<dependencyManagement>`** in parent POM. Versions defined once and inherited by all 8 services. |
| **Build & Compilation** | Must navigate into each folder and run `mvn clean install` 8 times. | **Single-Command Build:** Running `.\mvnw.cmd clean package` from root compiles and packages all 8 microservices at once! |

* **The Decision:** We adopted **Approach B: Maven Multi-Module Parent Project**.

---

### Decision 3: Inter-Service Communication Strategy (OpenFeign vs. RabbitMQ)

The case study document framed inter-service communication as an "either/or" choice:
> *"The application must demonstrate communication between microservices using either Option 1 (OpenFeign/WebClient) OR Option 2 (Kafka/RabbitMQ)."*

We analyzed the technical nature of the interactions and decided on an approach that is **better and more realistic than the basic requirement**:

```
┌────────────────────────────────────────────────────────────────────────┐
│                    INTER-SERVICE COMMUNICATION PARADIGMS               │
├──────────────────────────────────┬─────────────────────────────────────┤
│  SYNCHRONOUS (OpenFeign 📞)      │  ASYNCHRONOUS (RabbitMQ 📨)         │
│  "Like a Phone Call"             │  "Like a Text Message"              │
├──────────────────────────────────┼─────────────────────────────────────┤
│  • Service A calls Service B and │  • Service A fires an event into a  │
│    BLOCKS until it gets a reply. │    queue and IMMEDIATELY moves on.  │
│  • Used for Transactional Checks:│  • Used for Background Events:      │
│    When a candidate applies, the │    When an employer shortlists a    │
│    Application Service MUST know │    candidate, an event is published │
│    right now: "Is Job #10 valid  │    to notify the Messaging Service  │
│    and active?"                  │    in the background.               │
└──────────────────────────────────┴─────────────────────────────────────┘
```

* **The Decision:**
  1. **Primary Backbone:** Use **Spring Cloud OpenFeign** for all synchronous, request-response business transactions (validating employers before posting jobs, validating jobs before accepting applications).
  2. **Event-Driven Value-Add:** Once OpenFeign is active, introduce **RabbitMQ** specifically for asynchronous lifecycle events (e.g. `CandidateShortlistedEvent`), giving us a true enterprise hybrid architecture.

---

### Decision 4: Testing & Execution Strategy (Backend Momentum First)

* **Document Rule:** Testing using JUnit 5 and Mockito is mandatory.
* **Our Pragmatic Strategy (Codified in `antigravity.md`):**
  * We will **NOT** spend development time writing test suites for every microservice while building.
  * Writing tests while APIs are still in flux leads to brittle tests and slows development momentum.
  * **Execution:** Build the complete working backend (`src/main`) across all microservices first. Once all 8 services are operational and verified via Swagger UI, implement a comprehensive JUnit 5 + Mockito test suite for **one representative microservice** as the final capstone deliverable.

---

# 3. The Optimized 8-Microservice Ecosystem & Port Allocation Matrix

To prevent port conflicts and ensure predictable network routing, all 8 microservices are assigned fixed ports:

```
                                    ┌───────────────────────┐
                                    │    Clients / Postman  │
                                    └───────────┬───────────┘
                                                │
                                    ┌───────────▼───────────┐
                                    │ Spring Cloud Gateway  │  (Port 8080 - Single Entry Point)
                                    └───────────┬───────────┘
                                                │
                         ┌──────────────────────┴──────────────────────┐
                         ▼                                             ▼
             ┌───────────────────────┐                     ┌───────────────────────┐
             │ Eureka Service Server │ (Port 8761)         │  Auth Service (JWT)   │ (Port 8081)
             └───────────────────────┘                     └───────────────────────┘
                         │
      ┌──────────────────┼──────────────────┬──────────────────┐
      ▼                  ▼                  ▼                  ▼
┌───────────┐      ┌───────────┐      ┌───────────┐      ┌───────────┐
│ Employer  │      │ JobSeeker │      │    Job    │      │Application│
│  Service  │      │  Service  │      │  Service  │      │  Service  │
│(Port 8082)│      │(Port 8083)│      │(Port 8084)│      │(Port 8085)│
└───────────┘      └───────────┘      └───────────┘      └───────────┘
                         │                                     │
                         └──────────────────┬──────────────────┘
                                            ▼
                                      ┌───────────┐
                                      │ Messaging │
                                      │  Service  │
                                      │(Port 8087)│
                                      └───────────┘
```

| # | Service Name | Assigned Port | Database Name | Primary Responsibility |
| :--- | :--- | :--- | :--- | :--- |
| **1** | **`eureka-server`** | `8761` | None (In-Memory) | Central Service Registry. Tracks service instances dynamically. |
| **2** | **`api-gateway`** | `8080` | None | Unified client entry point, request routing, rate limiting, and JWT validation. |
| **3** | **`auth-service`** | `8081` | `auth_db` | User credentials, roles (`EMPLOYER`, `JOB_SEEKER`), and JWT token issuance. |
| **4** | **`employer-service`**| `8082` | `employer_db` | Organization registration, company profiles, and industry/location search. |
| **5** | **`jobseeker-service`**| `8083` | `jobseeker_db`| Candidate profiles, skills, education, experience, and resume management. |
| **6** | **`job-service`** | `8084` | `job_db` | Job postings, requirements, designation, salary ranges, and active status. |
| **7** | **`application-service`**| `8085`| `application_db` | Job applications and hiring workflow (`Applied` $\rightarrow$ `Shortlisted` $\rightarrow$ `Selected`). |
| **8** | **`messaging-service`**| `8087`| `messaging_db` | In-app messaging between employers and shortlisted candidates. |

---

# 4. The 8-Step Phased Build Sequence (From the Ground Up with Engineering Rationale)

In building microservices, **the order of construction is critical**. If you build components out of order, you run into missing dependencies, blocked API calls, and broken test environments.

Here is the exact step-by-step sequence and the engineering justification for each position:

---

### Step 0: The Parent Multi-Module Project (`job-search-system`)
* **What is built:** Root folder and master `pom.xml`. Configures Java 17/21, Spring Boot 3.3.x, Spring Cloud 2023.x dependency management, Lombok, and Maven Wrapper.
* **Why placed FIRST:** Every microservice is a Maven child module. Without the parent POM defining dependencies and versions, no sub-module can compile or inherit dependencies.

---

### Step 1: The Service Registry (`eureka-server` — Port 8761)
* **What is built:** Spring Cloud Netflix Eureka Server (`@EnableEurekaServer`).
* **Why placed SECOND:** In microservices, services **never hardcode IP addresses or port numbers** to talk to each other. They look up names (e.g. `http://employer-service/`) through Eureka.
* **Engineering Reason:** By having Eureka running first, every business microservice we create from Step 2 onwards will automatically register itself the moment it starts up!

---

### Step 2: The Core Independent Actors (`employer-service` & `jobseeker-service`)
* **What is built:**
  * **Step 2A:** `employer-service` (Port 8082) — Company profiles, contact person, location, and industry search.
  * **Step 2B:** `jobseeker-service` (Port 8083) — Candidate profile, skills, experience, and resume info.
* **Why placed THIRD:** Look at the dependency graph:
  * Does an Employer profile depend on any other microservice? **No.**
  * Does a Candidate profile depend on any other microservice? **No.**
* **Engineering Reason:** These two services represent the two fundamental actors in recruitment. You cannot have jobs without employers, and you cannot have applications without candidates. Building the independent actors first gives us a solid, unblocked base.

---

### Step 3: The Job Posting Engine (`job-service` — Port 8084)
* **What is built:** Job creation, description, salary ranges, required skills, and status management.
* **Why placed FOURTH:** A job posting **cannot exist without an Employer who created it**!
* **First Inter-Service Link (OpenFeign):**
  * When an employer posts a job (`POST /api/jobs`), `job-service` uses **OpenFeign** to call `employer-service`:
    ```java
    @FeignClient(name = "employer-service")
    public interface EmployerClient {
        @GetMapping("/api/employers/{id}")
        EmployerResponseDTO getEmployerById(@PathVariable("id") Long id);
    }
    ```
  * If Employer ID #1 does not exist, `job-service` rejects the request with HTTP 400!

---

### Step 4: The Interaction Engine (`application-service` — Port 8085)
* **What is built:** Application submission, status transitions (`Applied` $\rightarrow$ `Under Review` $\rightarrow$ `Shortlisted` $\rightarrow$ `Selected` / `Rejected`), and employer remarks.
* **Why placed FIFTH:** An application is a junction table connecting **a Candidate** to **a Job**.
* **Engineering Reason:** You literally **cannot build this service earlier**, because if Candidates (Step 2) and Jobs (Step 3) don't exist yet, there is nothing to apply for!
* **Dual OpenFeign Coordination:**
  * When applying, `application-service` uses OpenFeign to call:
    1. `jobseeker-service`: *"Is Candidate #5 registered and active?"*
    2. `job-service`: *"Is Job #10 open and accepting applications?"*
  * Only when both checks pass is the application created!

---

### Step 5: The Communication Layer (`messaging-service` — Port 8087)
* **What is built:** Messaging threads between employers and candidates tied to specific job IDs.
* **Why placed SIXTH:** Employers and candidates only communicate **after** an application has been submitted and reviewed.
* **Engineering Reason:** Placed after Application Service because messages reference `jobId`, `employerId`, and `jobSeekerId` established in Steps 2, 3, and 4. Also serves as the consumer for background notifications.

---

### Step 6: Authentication & Security (`auth-service` — Port 8081)
* **What is built:** User registration, password encryption (BCrypt), role assignment (`ROLE_EMPLOYER`, `ROLE_JOB_SEEKER`), and JWT token generation.
* **Why placed SEVENTH (Why not Day 1?):**
  * **Crucial Engineering Lesson:** If you lock down your APIs with JWT security on Day 1, *every single time you test any endpoint in Swagger or Postman during development, you must log in, copy a 300-character Bearer token, paste it into authorization headers, and manage token expirations every 15 minutes!*
  * It slows down feature development by 10x.
  * **Professional Strategy:** Build and verify all business logic first. Then introduce `auth-service` to manage credentials and issue JWT tokens for the completed ecosystem.

---

### Step 7: The Front Door (`api-gateway` — Port 8080)
* **What is built:** Spring Cloud Gateway. Single entry point for all external traffic.
* **Why placed EIGHTH:** The Gateway routes requests dynamically to downstream services discovered via Eureka:
  * `/api/employers/**` $\rightarrow$ `employer-service`
  * `/api/jobs/**` $\rightarrow$ `job-service`
  * `/api/applications/**` $\rightarrow$ `application-service`
  * `/api/messages/**` $\rightarrow$ `messaging-service`
* **Engineering Reason:** You cannot configure routing rules for services that don't exist yet! With all backend services operating, the Gateway ties the entire ecosystem together and acts as the centralized JWT filter.

---

### Step 8: The Capstone Wrap-Up
* **What is built:**
  1. Unified Swagger UI documentation aggregated across the Gateway.
  2. Master Postman Collection executing the complete recruitment journey end-to-end.
  3. Comprehensive Unit Testing Suite (JUnit 5 + Mockito) on one representative service (per directive in `antigravity.md`).

---

# 5. Summary & Next Execution Steps

### The Prework Blueprint is Complete:
* **Scope Finalized:** 8 microservices (Basket Service eliminated).
* **Project Structure Chosen:** Maven Multi-Module parent (`job-search-system`).
* **Communication Paradigm:** Spring Cloud OpenFeign for synchronous transactional integrity + optional RabbitMQ for asynchronous event notifications.
* **Build Sequence Locked:** 8-Step ground-up progression driven by dependency order.

---

### Next Immediate Action:
We are now ready to execute **Step 0 & Step 1**:
1. Create the parent folder `job-search-system/` and master `pom.xml`.
2. Implement **`eureka-server`** on Port `8761` and verify its dashboard in the browser!
