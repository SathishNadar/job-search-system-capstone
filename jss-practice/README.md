# Job Search System (JSS) - Employer Microservice (`jss-practice`)

This is the practice implementation of the **Employer Microservice** for the **Job Search System (JSS)** case study project, developed using **Spring Boot 3**, **Spring Data JPA**, **Bean Validation**, **Springdoc OpenAPI (Swagger)**, and **JUnit 5 / Mockito**.

---

## 1. Features & Architectural Highlights

* **Layered Architecture**: Strict separation of concerns across Controller, Service, Repository, DTO, and Entity layers.
* **DTO Pattern**: Decouples external API models from internal JPA entities (`EmployerRequestDTO` & `EmployerResponseDTO`).
* **Bean Validation**: Validates user inputs with descriptive error messages (`@NotBlank`, `@Email`, `@Pattern`, `@Size`).
* **Global Exception Handling**: Centralized `@RestControllerAdvice` generating standard error responses matching the case study document specification:
  ```json
  {
    "timestamp": "2026-11-16T10:00:00",
    "status": 404,
    "error": "Not Found",
    "message": "Employer not found with id: 999",
    "path": "/api/employers/999"
  }
  ```
* **API Documentation**: Interactive **Swagger UI** available out-of-the-box at `http://localhost:8081/swagger-ui.html`.
* **Zero-Setup Database**: In-memory **H2 Database** pre-seeded with sample records on application startup; seamlessly switchable to PostgreSQL via `application.properties`.
* **Complete Test Suite**: Unit tests using **JUnit 5** and **Mockito** covering Service and Controller layers across positive and negative paths.
* **Ready-to-Use Postman Collection**: `JSS_Employer_Service.postman_collection.json` included in root.

---

## 2. Project Directory Structure

```
jss-practice/
├── pom.xml                                  <-- Maven build configuration & dependencies
├── mvnw.cmd / mvnw                          <-- Maven wrapper scripts (no pre-installed Maven needed)
├── JSS_Employer_Service.postman_collection.json <-- Ready-to-import Postman test suite
├── src/
│   ├── main/
│   │   ├── java/com/jss/employer/
│   │   │   ├── EmployerServiceApplication.java <-- Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java         <-- Swagger / OpenAPI 3 metadata
│   │   │   │   └── DataInitializer.java       <-- Sample seed data on startup
│   │   │   ├── controller/
│   │   │   │   └── EmployerController.java    <-- REST endpoints with Swagger annotations
│   │   │   ├── dto/
│   │   │   │   ├── EmployerRequestDTO.java    <-- Input validation rules
│   │   │   │   └── EmployerResponseDTO.java   <-- API response structure
│   │   │   ├── entity/
│   │   │   │   └── Employer.java              <-- JPA Entity (all 13 fields from doc)
│   │   │   ├── exception/
│   │   │   │   ├── EmployerNotFoundException.java
│   │   │   │   ├── DuplicateEmployerException.java
│   │   │   │   ├── ErrorResponse.java         <-- Document-mandated error format
│   │   │   │   └── GlobalExceptionHandler.java<-- @RestControllerAdvice
│   │   │   ├── repository/
│   │   │   │   └── EmployerRepository.java    <-- Spring Data JPA repository
│   │   │   └── service/
│   │   │       ├── EmployerService.java       <-- Business service interface
│   │   │       └── impl/EmployerServiceImpl.java <-- Implementation & validations
│   │   └── resources/
│   │       └── application.properties         <-- Port 8081, H2 & Swagger configs
│   └── test/
│       └── java/com/jss/employer/
│           ├── controller/
│           │   └── EmployerControllerTest.java <-- MockMvc endpoint & validation tests
│           └── service/
│               └── EmployerServiceTest.java    <-- Mockito service unit tests
```

---

## 3. How to Run the Application

### Option A: Using the Integrated Terminal (Any IDE / Command Line)
Navigate to the `jss-practice` folder and run:
```powershell
.\mvnw.cmd spring-boot:run
```
*(On Linux/macOS/Git Bash: `./mvnw spring-boot:run`)*

### Option B: In Spring Tool Suite (STS) / Eclipse
1. Open STS.
2. Select **File -> Import -> Maven -> Existing Maven Projects**.
3. Browse and select the `jss-practice` folder, then click **Finish**.
4. Right-click on the project root -> **Run As -> Spring Boot App**.

### Option C: In Antigravity IDE / VS Code
1. Open the `jss-practice` folder.
2. Open `src/main/java/com/jss/employer/EmployerServiceApplication.java`.
3. Click the **Run** button above the `main` method (or press `F5`).

---

## 4. How to Verify & Test

Once started on **port 8081**:

| Tool / Resource | URL / Location | Description |
| :--- | :--- | :--- |
| **Swagger UI** | [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) | Interactive web UI to execute all API calls |
| **OpenAPI Spec (JSON)** | [http://localhost:8081/api-docs](http://localhost:8081/api-docs) | Raw OpenAPI v3 specification |
| **H2 Web Console** | [http://localhost:8081/h2-console](http://localhost:8081/h2-console) | Database console (JDBC URL: `jdbc:h2:mem:employerdb`, User: `sa`, Password: *empty*) |
| **Postman** | `JSS_Employer_Service.postman_collection.json` | Import into Postman to run pre-made test requests |

---

## 5. Running Automated Unit Tests

To run all JUnit 5 and Mockito tests:
```powershell
.\mvnw.cmd test
```
All tests verify positive paths, negative paths (404 Not Found, 409 Conflict), and bean validation failures (400 Bad Request).
