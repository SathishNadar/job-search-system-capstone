# Antigravity Instructions & Persistent Directives (JSS Project)

> **File Purpose:** This file records all persistent user preferences, architectural rules, and standing instructions to be strictly carried forward throughout the development of the **Job Search System (JSS)** and all its microservices.

---

## 1. Core Code Generation & Style Directives

### A. Project Lombok (MANDATORY for the Actual JSS Project)
* **User Directive:** In the actual main JSS project, **always use Project Lombok**. Do **NOT** write explicit getters, setters, or constructors.
* **Standard Annotations to Use:**
  * Entities & DTOs: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
  * For Entities with specific needs: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`.
  * Slf4j Logging: Use `@Slf4j` for logger instantiation (`log.info()`, `log.error()`).

### B. OpenAPI / Swagger Documentation (`@Schema`)
* **User Directive:** Retain `@Schema` annotations on all DTOs and Controllers.
* Always supply:
  * `description = "..."`: Clear explanation of the field for frontend engineers.
  * `example = "..."`: Realistic, pre-validated dummy data for instant 1-click execution in Swagger UI.

### C. Database Architecture: Local MySQL ONLY (No H2, No PostgreSQL)
* **User Directive:** For the actual project (`jss-capstone`), **strictly use local MySQL**.
* **NO H2 Database:** H2 was only for initial practice/prototyping; the actual project must persist to local MySQL.
* **NO PostgreSQL:** Replace the document's PostgreSQL mention with MySQL (`com.mysql:mysql-connector-j`).
* **Workbench Accessibility:** Databases must be created and inspectable directly in **MySQL Workbench**.
* **Auto-creation:** Connection strings should include `createDatabaseIfNotExist=true` so MySQL creates the database automatically on startup if missing.

### D. Layered Microservice Blueprint
For every microservice created in the project, strictly follow this standard structure:
1. **`entity/`**: JPA Entity mapped to database table.
2. **`dto/`**: `RequestDTO` (with Bean Validation) and `ResponseDTO` (using Lombok).
3. **`repository/`**: Interface extending `JpaRepository<Entity, ID>` with derived query methods.
4. **`exception/`**: Custom exceptions + `GlobalExceptionHandler` (`@RestControllerAdvice`) returning the standard error format:
   ```json
   {
     "timestamp": "2026-11-17T10:00:00",
     "status": 400,
     "error": "Bad Request",
     "message": "Validation Failed - ...",
     "path": "/api/..."
   }
   ```
5. **`service/` & `service/impl/`**: Interface + Implementation class. Use helper methods or mapper utilities for clean conversion.
6. **`controller/`**: REST controller with `@Tag`, `@Operation`, `@Valid`, returning standard HTTP status codes (`201`, `200`, `204`).
7. **`config/`**: Swagger OpenAPI config bean and seed data initializers.

### E. Testing Policy for the Actual Project (Backend First, Tests at the End)
* **User Directive:** Do **NOT** create `src/test` folders, test classes, or Mockito tests when creating microservices during development.
* **Focus:** 100% of effort goes into building the working production features (`src/main`): Entity $\rightarrow$ DTO $\rightarrow$ Repository $\rightarrow$ Service $\rightarrow$ Controller $\rightarrow$ Swagger UI.
* **Testing Execution:** Unit testing (JUnit 5 + Mockito) will be implemented for **only ONE clean, representative microservice** at the very end after all backend microservices are fully developed, connected, and functioning.

### F. Git Commit & Push Policy (Strictly User-Triggered)
* **User Directive:** **NEVER commit or push code automatically.**
* **Working Tree State:** After implementing a microservice or step, leave all newly created files uncommitted in the local working directory.
* **User Prompted Only:** When a microservice is fully verified, the user will explicitly ask for modular commits.
* **Execution:** Only provide the modular commit commands/messages when the user prompts for it, and allow the user to push it to remote themselves. Do not run git push autonomously.

---

## 2. Interactive Pre-Implementation & Educational Protocol (The 2-Gate Permission Rule)

Whenever we are about to design and create a new step or microservice in the actual project, we must **NEVER** write code upon a single command. We must strictly follow this **2-Gate Protocol**:

### Gate 1: Comprehensive Step Documentation & Architectural Plan
Before writing any code or modifying the build:
1. **Create a dedicated Step Documentation Markdown file** (e.g., `STEP_1_EUREKA_SERVER_GUIDE.md`).
2. **The document must include:**
   * **Feature Overview:** What this step implements and what it brings to the microservice ecosystem.
   * **Folder & File Inventory:** Every file and folder to be created, and the precise responsibility of each.
   * **Core Technical Concepts & Connection Mechanics:** Illustrated with code snippets showing how services interact (e.g. how Eureka acts as a phonebook, how clients register, heartbeats).
   * **New Annotations & Java Constructs:** Detailed explanation of annotations used (e.g. `@EnableEurekaServer`) so the user understands the exact syntax.
   * **Verification Strategy:** Exactly how we will verify the step is working (e.g., browser URLs, dashboards).

### Gate 2: Theoretical Alignment & Explicit User Permission
Once Gate 1 documentation is created:
1. The assistant must present the plan and explicitly ask:
   * **Question A (Understanding Check):** Has the user reviewed the guide and completely understood the theoretical concepts, code snippets, and annotations?
   * **Question B (Implementation Permission):** Does the user approve the plan and give explicit permission to proceed with code generation?
2. **Strict Rule:** **ONLY when the user explicitly answers YES to both checks** does the assistant proceed to generate and execute the actual implementation code!

### Step-Specific Doubt Solving Rule (MANDATORY)
* **Directive:** Do **NOT** modify or update `jss-practice/SPRING_CONCEPTS_AND_DOUBTS.md` for doubts in the capstone project. That file belongs strictly to `jss-practice`.
* For all capstone microservices and steps, all questions and doubts asked by the user must be appended directly to the bottom of that specific step's documentation file (e.g. `STEP_1_EUREKA_SERVER_GUIDE.md`) inside a dedicated section:
  ```markdown
  ## Doubt Solving & Conceptual Deep-Dives
  ```
* This keeps every microservice self-documented with its own Q&A knowledge base!

---

### Step A: Interactive API Proposal & Review (For Domain Services)
Before writing any controller or service code:
1. **List all candidate APIs** for the microservice (HTTP method, endpoint URI, request payload, response schema).
2. **Classify by criticality**:
   * **Tier 1 (Mandatory / Core)**: Essential for base CRUD operations.
   * **Tier 2 (Advanced / Value-Add)**: Complex search, multi-field filters, sorting, bulk actions, status transitions.
3. **Provide Real-World Scenario Examples**:
   * Explain *why* the endpoint exists and how a real client/frontend uses it.
4. **Pause for User Alignment**:
   * Wait for the user to review, modify parameters, select which APIs to keep, and discard unwanted endpoints.

### Step B: Interactive Exception Handling & Error Scenario Review (For Domain Services)
Before writing any exception classes or handler advice:
1. **List all $N$ potential exceptions** that could logically occur in this microservice.
2. **Explain the significance of each exception**:
   * What business or validation rule triggered it?
   * What HTTP status code should it return (400, 404, 409, 422)?
3. **Provide Concrete Trigger Scenarios**:
   * Walk through a specific scenario where this exception prevents data corruption or guides the user (e.g., trying to shortlist a candidate whose application is already `Rejected`, or updating to an email that belongs to another company).
4. **Pause for User Alignment**:
   * Discuss and finalize with the user which exceptions are strictly needed, which can be merged into generic validation, and which to discard.

---

## 3. Technology Stack & Framework Rules

| Component | Standard / Technology | Notes |
| :--- | :--- | :--- |
| **Java Version** | Java 17 / 21+ compatible | Standard modern Spring Boot 3.x |
| **Build Tool** | Maven (`pom.xml` + `mvnw.cmd`) | Self-bootstrapping Maven wrapper |
| **Persistence** | Spring Data JPA + Hibernate | `ddl-auto=update` |
| **Database** | **Local MySQL Database ONLY** (`com.mysql:mysql-connector-j`) | Accessible via MySQL Workbench. **NO H2, NO PostgreSQL** in actual project. |
| **Documentation** | Springdoc OpenAPI 3 (Swagger UI) | Accessible at `/swagger-ui.html` |
| **Boilerplate Reduction** | Project Lombok | Required for actual JSS project |

---

## 4. Decision Log & Future Directives Tracker

| Date | Directive / Decision | Requested By | Action Taken |
| :--- | :--- | :--- | :--- |
| **2026-11-18** | **Strict Git Commit & Push Rule**: Never automatically commit or push code. Leave all files uncommitted in the working tree. Only provide modular commit breakdowns when the user explicitly prompts for it. Added to Section 1.F. | User | Reset local commit; logged as strict protocol across all future steps. |
| **2026-11-18** | **Local MySQL Database Requirement**: Use local MySQL database exclusively across all microservices (no H2, no PostgreSQL). Must be inspectable via MySQL Workbench. Added to Section 1.C. | User | MySQL driver & configuration mandated across all services. |
| **2026-11-18** | **Step 1 Completed (`eureka-server`)**: Implemented Eureka Discovery Server on port 8761 with `@EnableEurekaServer` and `application.yml`. Registered child module in parent `pom.xml`. Verified via 2-Gate Protocol. | User | Discovery server module ready. |
| **2026-11-18** | **Step-Specific Doubt Solving Rule**: Do NOT update `jss-practice/SPRING_CONCEPTS_AND_DOUBTS.md`. Add all doubts and explanations directly to the bottom of that step's guide (e.g. `STEP_1_EUREKA_SERVER_GUIDE.md`) under `## Doubt Solving & Conceptual Deep-Dives`. | User | Codified in Section 2 and enforced across all steps. |
| **2026-11-18** | **Two-Gate Pre-Implementation Protocol Enacted**: Gate 1: Comprehensive Step Guide MD with architecture, folder breakdown, code snippets & annotations. Gate 2: Explicit theoretical understanding check & implementation permission from user before writing any code. | User | Codified in Section 2. Mandatory for all steps starting with Step 1. |
| **2026-11-18** | **Step 0 Completed (`jss-capstone`)**: Initialized Maven Multi-Module root project with parent `pom.xml`, Spring Cloud 2023.0.3, Lombok, and Maven Wrapper. | User | Master parent project ready. |
| **2026-11-18** | **Prework Architecture Blueprint Approved**: 8-Service fleet (Basket Service eliminated), Maven Multi-Module parent, OpenFeign primary communication, 8-Step phased build sequence. Recorded in `PROJECT_PREWORK_AND_ARCHITECTURE.md`. | User | Blueprint finalized and documented. |
| **2026-11-18** | **No Test Files During Development**: Focus exclusively on `src/main` backend development for all microservices. Add JUnit 5 / Mockito tests to only ONE representative microservice at the very end after all backend work is complete. | User | Logged as standard testing policy in Section 1.D. |
| **2026-11-17** | **Interactive Pre-Implementation Protocol**: For every new microservice, must present, explain, and review all potential APIs and Exceptions with real-world scenarios before writing code. User decides what to keep/discard. | User | Embedded as mandatory Step A & Step B protocol in Section 2. |
| **2026-11-17** | **Use Lombok in Actual JSS Project**: Eliminate explicit getters, setters, and constructors in DTOs and Entities using `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`. | User | Logged as mandatory rule for main project generation. |
| **2026-11-17** | **Keep `@Schema` with examples**: Retain OpenAPI `@Schema` annotations on DTOs for 1-click Swagger testing. | User | Confirmed and retained in blueprint. |
| **2026-11-17** | **Maintain `SPRING_CONCEPTS_AND_DOUBTS.md`**: Continuously update the learning notes as new questions are asked. | User | Maintained inside `jss-practice/` and updated per doubt. |
