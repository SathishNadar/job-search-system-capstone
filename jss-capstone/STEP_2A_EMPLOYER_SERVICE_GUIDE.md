# Step 2A: Employer Microservice (`employer-service`) — Architectural & API Proposal Guide

> **Module Name:** `employer-service`  
> **Assigned Port:** `8082`  
> **Registry Integration:** Registers automatically with `eureka-server` (Port `8761`)  
> **Status:** Gate 1 Proposal & Architectural Alignment Phase (No code generated yet)

---

## 1. Feature Overview & Role in the Ecosystem

In the Job Search System, the **Employer Microservice** represents the hiring organization. It is the first core business domain service we build because:
1. **Zero External Dependencies:** An employer company profile does not depend on any other microservice to exist.
2. **Prerequisite for Jobs:** Job postings (Step 3) cannot exist without a valid `employerId`.
3. **First Eureka Client:** This service will be our very first microservice to dynamically register itself with the **Eureka Discovery Server** on port `8761`.

---

## 2. File & Folder Inventory: What Will Be Created

Unlike the practice project, this actual capstone service **strictly uses Project Lombok** across all Entities and DTOs per our project directives (no manual getters, setters, or constructors).

```
jss-capstone/
├── pom.xml                                         <-- Register <module>employer-service</module>
└── employer-service/                               <-- NEW SUB-MODULE FOLDER
    ├── pom.xml                                     <-- Child POM (Web, JPA, Eureka Client, Lombok, MySQL Driver)
    └── src/
        └── main/
            ├── java/com/jss/employer/
            │   ├── EmployerServiceApplication.java <-- Spring Boot entry point
            │   ├── config/
            │   │   ├── OpenApiConfig.java          <-- Swagger UI metadata on port 8082
            │   │   └── DataInitializer.java        <-- Seed realistic company data into MySQL
            │   ├── controller/
            │   │   └── EmployerController.java     <-- Finalized REST endpoints
            │   ├── dto/
            │   │   ├── EmployerRequestDTO.java     <-- Input payload (Lombok + Bean Validation)
            │   │   └── EmployerResponseDTO.java    <-- Output payload (Lombok)
            │   ├── entity/
            │   │   └── Employer.java               <-- JPA Entity (Lombok + @Entity)
            │   ├── exception/
            │   │   ├── ErrorResponse.java          <-- Standard JSON error schema
            │   │   ├── GlobalExceptionHandler.java <-- @RestControllerAdvice
            │   │   ├── EmployerNotFoundException.java <-- 404
            │   │   ├── DuplicateEmployerException.java <-- 409
            │   │   └── CannotDeleteEmployerWithActiveJobsException.java <-- Template for future job-service integration
            │   ├── repository/
            │   │   └── EmployerRepository.java     <-- Spring Data JPA derived queries
            │   └── service/
            │       ├── EmployerService.java        <-- Interface
            │       └── impl/EmployerServiceImpl.java <-- Business logic & mapping
            └── resources/
                └── application.properties          <-- Port 8082, Eureka URL, Local MySQL (employer_db)
```

---

## 3. Step A: Interactive API Proposal & Review

Here is the complete menu of candidate APIs we can implement for `employer-service`. Please review them so we can decide which to keep, modify, or discard:

### Tier 1: Core / Mandatory APIs (Essential for Base CRUD)

| Method | Endpoint URI | Request Body | Response Body | Status Code | Real-World Scenario |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`POST`** | `/api/employers` | `EmployerRequestDTO` | `EmployerResponseDTO` | `201 Created` | **Company Onboarding:** A new company (e.g. Google India) registers its profile on the platform. Enforces validation and duplicate email prevention. |
| **`GET`** | `/api/employers/{id}` | *None* | `EmployerResponseDTO` | `200 OK` | **Profile Lookup:** A candidate or admin clicks on a company card to view its full details (address, website, industry, contact person). |
| **`PUT`** | `/api/employers/{id}` | `EmployerRequestDTO` | `EmployerResponseDTO` | `200 OK` | **Profile Update:** The employer updates its office address, company size, or website. Performs smart email conflict checks. |
| **`DELETE`** | `/api/employers/{id}` | *None* | `Map<String, String>` | `200 OK` | **Account Removal:** An employer chooses to close their account and remove their company profile from the platform. |

---

### Tier 2: Advanced / Value-Add APIs (For Your Decision)

#### Option 1 vs. Option 2 for `getAllEmployers`:
* **Option 1 (Simple Listing):** `GET /api/employers`  
  * *What it does:* Returns a list of all employers in the system without any filtering options.
  * *Pros:* Very simple code, easy to test.
  * *Cons:* Not practical if there are 500 companies in the database.
* **Option 2 (Parameterized Filter Listing — Recommended):** `GET /api/employers?industryType=...&city=...`  
  * *What it does:* Allows fetching all employers, OR filtering by `industryType` (e.g. `IT`), `city` (e.g. `Bengaluru`), or both simultaneously.
  * *Real-World Scenario:* A job seeker wants to browse only IT companies located in Bengaluru.

#### Additional Specialized Endpoints (Keep or Discard?):
* **API 6: Search by Organization Name:** `GET /api/employers/search?name=...`  
  * *Scenario:* Autocomplete search bar on the frontend where a candidate types `"Capg"` and it matches `"Capgemini Technology Services"`.
* **API 7: Verify Employer Exists (Internal OpenFeign Endpoint):** `GET /api/employers/{id}/exists`  
  * *Scenario:* Lightweight check returning `{ "exists": true }`. Used by `job-service` before posting a job without needing to fetch the full 13-field employer profile.

---

## 4. Step B: Interactive Exception Handling & Error Scenario Review

Here are the potential exceptions that could occur in `employer-service`. Review each scenario so we can finalize which exceptions we will build:

### Exception 1: `EmployerNotFoundException` (HTTP 404 Not Found)
* **What triggers it:** A client requests, updates, or deletes an employer ID that does not exist in the database (e.g., `GET /api/employers/999`).
* **Scenario:** A candidate clicks on an outdated link to a company that has deleted its profile.
* **Significance:** Informs the client cleanly that the resource does not exist rather than returning a blank 200 or crashing with 500.
* **Recommendation:** **MANDATORY (KEEP)**.

### Exception 2: `DuplicateEmployerException` (HTTP 409 Conflict)
* **What triggers it:** A user attempts to register a company with an email address that is already registered to another employer, or an employer attempts to update their email to one already owned by another company.
* **Scenario:** An HR representative accidentally tries to register `"careers@google.com"` a second time.
* **Significance:** Protects database integrity and prevents duplicate business accounts.
* **Recommendation:** **MANDATORY (KEEP)**.

### Exception 3: `ValidationException` / `MethodArgumentNotValidException` (HTTP 400 Bad Request)
* **What triggers it:** Bean Validation failure on incoming payload (e.g., blank organization name, invalid email format, 3-digit phone number).
* **Scenario:** An employer submits the registration form without filling in the mandatory company name.
* **Significance:** Returns immediate field-level error messages (`"Organization name cannot be blank"`) so the user knows exactly what to fix.
* **Recommendation:** **MANDATORY (KEEP)**.

### Exception 4: `EmployerAccountDeactivatedException` (HTTP 403 Forbidden - Optional)
* **What triggers it:** Trying to update or post jobs for an employer whose account status is `INACTIVE` or `SUSPENDED`.
* **Scenario:** An employer whose account was temporarily suspended tries to edit their profile.
* **Decision needed:** Do you want an account status check (`ACTIVE`/`INACTIVE`), or is this unnecessary complexity for now?

### Exception 5: `CannotDeleteEmployerWithActiveJobsException` (HTTP 409 Conflict - Optional)
* **What triggers it:** Trying to delete an employer profile who still has active job postings in the system.
* **Scenario:** An admin attempts to delete a company profile, but candidates are actively interviewing for their open jobs.
* **Decision needed:** We can either implement this later when `job-service` exists, or keep deletion simple for now.

---

## 5. Technical Highlights for this Step

### A. Project Lombok (Cleaner Code)
In `Employer.java`:
```java
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
    private String organizationName;
    ...
}
```
* Reduces the class from 140 lines to just **35 clean lines**!

### B. Eureka Client Registration (`application.properties`)
```properties
server.port=8082
spring.application.name=employer-service

# Tells the microservice where the Eureka Discovery Server lives:
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

---

## 6. Verification Plan

1. Start `eureka-server` (Port `8761`).
2. Start `employer-service` (Port `8082`).
3. Open `http://localhost:8761` $\rightarrow$ Confirm **`EMPLOYER-SERVICE`** appears in the Eureka instances list!
4. Open Swagger UI at `http://localhost:8082/swagger-ui.html` $\rightarrow$ Test the finalized endpoints.

---

## 7. Doubt Solving & Conceptual Deep-Dives

### Doubt 1: Where is API 6 (Search by Name: `GET /api/employers/search?name=...`) actually used? Can a candidate use it to filter jobs by company?

#### The Real-World Frontend Use Case:
1. **The Autocomplete Search Bar:**  
   Imagine the homepage of the Job Search System. There is a search bar: *"Search by Company Name"*.
   * As the candidate types `"Capg"`, the frontend calls: `GET /api/employers/search?name=Capg`.
   * It returns `"Capgemini Technology Services"` (with `employerId: 1`).
2. **Connecting to Jobs:**  
   Can this be used to view jobs by that specific company? **YES!**
   * The candidate clicks on `"Capgemini"` in the search results.
   * The frontend grabs `employerId: 1` and calls `job-service`:
     ```
     GET /api/jobs?employerId=1
     ```
   * Result: The candidate sees all active job postings published specifically by Capgemini!

---

### Doubt 2: Where are the `city` and `industryType` filters in `getAllEmployers` used?

#### The Real-World Frontend Use Case:
On job platforms (like LinkedIn or Indeed), there is a **"Browse Companies" / "Companies Directory"** page:
1. **Default View (No parameters):**  
   Calling `GET /api/employers` lists all registered companies so a candidate can browse alphabetically.
2. **Targeted Filtering (With parameters):**  
   A candidate specifically wants to work in **Fintech in Mumbai**:
   * They select dropdowns: Industry = `Banking & Financial Services`, City = `Mumbai`.
   * Frontend calls:
     ```
     GET /api/employers?industryType=Banking%20&%20Financial%20Services&city=Mumbai
     ```
   * Result: Returns only HDFC Bank, ICICI Bank, etc., eliminating all irrelevant companies.

---

### Doubt 3: Should employers be allowed to delete their accounts? Do we need `CannotDeleteEmployerWithActiveJobsException`?

#### The Architectural Reality:
* **Can employers delete their accounts?**  
  Yes, platforms allow account deletion (for company shutdowns, duplicate accounts, or privacy compliance).
* **The Dangerous Problem with Deletion:**  
  What happens if an employer posted 5 jobs, and 50 candidates have applied and are interviewing?
  * If the employer is deleted from the database, what happens to their jobs? The jobs become **"orphaned"** (jobs with an invalid/missing company).
  * When candidates check their application status, the company name crashes or shows `null`!
* **Do we need `CannotDeleteEmployerWithActiveJobsException` in Step 2A?**  
  * **User Decision:** Create a **template exception class** for it in `com.jss.employer.exception` with full Javadoc explaining its future role.
  * **Wiring:** Keep it **unwired** for now in `EmployerServiceImpl.java` because `job-service` has not been implemented yet. Once `job-service` and OpenFeign dependencies are born (Step 3), this exception will be wired into the delete flow!

---

### Doubt 4: Why do we need `?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`? Can't Hibernate create the database with a normal URL?

#### 1. The Core Distinction: Database (Schema) vs. Table
* **`employer_db`** is the **Database (Schema)**.
* **`employers`** is the **Table** inside that database.

#### 2. What Hibernate Can and CANNOT Do:
* Hibernate's property `spring.jpa.hibernate.ddl-auto=update` is responsible **strictly for TABLES**.
* Hibernate connects to an *already existing* database and executes:
  ```sql
  CREATE TABLE IF NOT EXISTS employers (...);
  ```
* **Hibernate CANNOT create the database itself!** If `employer_db` does not exist on your MySQL server, Hibernate cannot even establish a connection, crashing immediately on startup with:
  ```
  java.sql.SQLSyntaxErrorException: Unknown database 'employer_db'
  ```

#### 3. Why Each Parameter is Required for Local MySQL:
1. **`createDatabaseIfNotExist=true`:**
   * This is handled by the **MySQL JDBC Driver** (not Hibernate).
   * Before handing the connection to Hibernate, the driver checks if `employer_db` exists. If not, it executes:
     ```sql
     CREATE DATABASE IF NOT EXISTS employer_db;
     ```
   * *Benefit:* You never have to manually open MySQL Workbench and write `CREATE DATABASE employer_db;` before launching a new microservice.
2. **`useSSL=false`:**
   * Local development on your laptop does not have an SSL/TLS security certificate on `localhost:3306`.
   * Without this, MySQL driver throws SSL handshake warnings or connection failures.
3. **`serverTimezone=UTC`:**
   * Resolves timezone differences between your machine (e.g. IST) and the MySQL server.
   * Without this, MySQL 8 often throws `SQLException: The server time zone value 'IST' is unrecognized`.
4. **`allowPublicKeyRetrieval=true`:**
   * MySQL 8+ uses `caching_sha2_password` authentication by default.
   * When connecting locally without an SSL certificate, MySQL requires explicit client permission to retrieve the server's RSA public key to encrypt and hash the password.
   * Without this parameter, Spring Boot crashes on boot with:
     ```
     java.sql.SQLNonTransientConnectionException: Public Key Retrieval is not allowed
     ```

---

### Doubt 5: What do `prefer-ip-address=true` and `instance-id` actually do? Isn't `defaultZone` enough?

In `application.properties`:
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.instance-id=${spring.application.name}:${server.port}:${random.value}
```

#### 1. Property 1: `defaultZone` (Where is the phonebook?)
* Tells `employer-service` where Eureka lives (`localhost:8761`).
* **Why it is not enough on its own:** It tells the client *where to go*, but it doesn't control *how the client describes itself* to Eureka or *how other microservices will reach it*.

#### 2. Property 2: `prefer-ip-address=true` (How should other services reach me?)
* **Default Behavior (if omitted / false):** Eureka registers the service by its operating system **hostname** (e.g. `LAPTOP-89AB12CD` or `sathish-workstation`).
* **The Problem with Hostnames:**
  * In corporate environments, VPNs (like Capgemini VPN), Docker containers, or cloud VMs, hostname DNS resolution often fails.
  * When `job-service` asks Eureka for `employer-service`, Eureka returns `http://LAPTOP-89AB12CD:8082`. `job-service` cannot resolve that name and crashes with `java.net.UnknownHostException`!
* **The Fix:** Setting `prefer-ip-address=true` forces Eureka to register the numeric IP address (e.g. `http://192.168.1.5:8082` or `http://127.0.0.1:8082`), which always resolves reliably without DNS issues.

#### 3. Property 3: `instance-id` (How do we distinguish multiple copies?)
* **Default Behavior (if omitted):** Eureka creates an ID using just the hostname and app name.
* **The Problem:**
  * If you run **2 instances** of `employer-service` on your machine for load balancing (e.g. one on port 8082 and another on port 8083), with default settings the second instance **overwrites** the first one in Eureka! You can never run or see multiple copies side-by-side.
  * Also, default IDs on the Eureka Dashboard look like confusing machine strings: `LAPTOP-XYZ:employer-service:8082`.
* **The Fix:** `${spring.application.name}:${server.port}:${random.value}` creates a clean, unique label:
  ```
  employer-service:8082:a8f9c1
  ```
  * **Result:** On `http://localhost:8761`, you immediately see the service name, exact port, and unique process ID at a glance, and multiple instances can scale without colliding!

---

### Doubt 6: What is Lombok's `@Builder`? Why is it used here and what does it do?

#### 1. What is `@Builder`?
Lombok's `@Builder` annotation automatically generates the **Builder Design Pattern** for your Java class at compile time.

#### 2. The Problem with Traditional Constructors ("Constructor Hell"):
`EmployerResponseDTO` and `Employer` have **13 fields**:
```
employerId, organizationName, contactPerson, address, city, state, country, contactNumber, email, website, industryType, companySize, createdDate
```
In traditional Java, you would have to instantiate it like this:
```java
EmployerResponseDTO dto = new EmployerResponseDTO(
    1L, "Google India Pvt Ltd", "Sundar Pichai", "RMZ Infinity", 
    "Bengaluru", "Karnataka", "India", "9876543210", 
    "careers@google.com", "https://careers.google.com", 
    "Information Technology", "10000+ Employees", LocalDateTime.now()
);
```
* **Dangerous Bug Risk:** There are 10 consecutive `String` parameters! If you accidentally swap `city` and `state`, or `contactPerson` and `organizationName`, Java will **not** complain (both are `String`). You introduce silent data bugs in production!
* **Rigid Parameter Order:** You have to remember the exact position of all 13 variables.
* **All-or-Nothing:** If you only want to set 3 fields (e.g. in tests or partial mappings), you must pass `null, null, null` for all other 10 fields!

#### 3. How `@Builder` Solves This:
With `@Builder`, Lombok creates a fluent API where you chain method calls named directly after the fields:
```java
EmployerResponseDTO response = EmployerResponseDTO.builder()
        .employerId(entity.getEmployerId())
        .organizationName(entity.getOrganizationName())
        .email(entity.getEmail())
        .city(entity.getCity())
        .build();
```

#### 4. Why is `@Builder` used across `employer-service`?
1. **In `EmployerServiceImpl`:** Cleanly maps entities to response DTOs and request DTOs to entities without confusing parameter orders.
2. **In `DataInitializer`:** Seeds sample companies (Capgemini, Google India, HDFC) with clear, self-documenting code.
3. **In `GlobalExceptionHandler`:** Easily constructs standardized `ErrorResponse` objects by chaining `.status(...)`, `.message(...)`, `.path(...)`, and `.build()`.
4. **Order Independence:** You can specify fields in any order you want.
5. **Readability:** Any developer reading `.email("...")` immediately knows which field is being set.

---

### Doubt 7: Why create a dedicated `ErrorResponse` class? Couldn't we just return a plain String or a `Map<String, Object>` inside `GlobalExceptionHandler`?

#### 1. What Could We Do Instead?
Instead of a dedicated class, we *could* have written:
```java
// Alternative A: Plain String
return ResponseEntity.status(404).body("Employer not found with ID: " + id);

// Alternative B: Raw Map
Map<String, Object> error = new HashMap<>();
error.put("status", 404);
error.put("message", ex.getMessage());
return ResponseEntity.status(404).body(error);
```

#### 2. Why Professional Microservices Use a Dedicated `ErrorResponse` Class:

##### A. The Frontend / Client Contract (Single Universal Error Interceptor)
In a microservices system, there are 8 services (`employer-service`, `job-service`, `auth-service`, etc.).
* If every service or handler invents its own error shape:
  * `employer-service` returns: `{"errorMsg": "Not found"}`
  * `job-service` returns a plain String: `"Job 5 does not exist"`
  * `auth-service` returns: `{"err": "Invalid token", "code": 401}`
* **The Frontend Nightmare:** The frontend developer (React/Angular) would have to write custom parsing code for every single microservice!
* **The Solution:** With a shared `ErrorResponse` format, the frontend writes a **single universal interceptor**:
  ```javascript
  axios.interceptors.response.use(
    response => response,
    error => {
      // Guaranteed to exist on every error across all 8 microservices!
      toast.error(error.response.data.message); 
    }
  );
  ```

##### B. Type Safety vs. Map Typos
When using `Map<String, Object>`:
* A developer could easily type `error.put("mesage", ...)` or `error.put("timestamp", ...)` in one method and `error.put("time", ...)` in another.
* The Java compiler cannot catch typos in map keys! The bug is only discovered when the frontend crashes.
* With `ErrorResponse`, the compiler strictly enforces that only `timestamp`, `status`, `error`, `message`, and `path` can be populated.

##### C. Interactive Swagger / OpenAPI Documentation
In `EmployerController`:
```java
@ApiResponse(responseCode = "404", description = "Employer not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
```
Swagger UI inspects the `ErrorResponse` class and generates an interactive, readable JSON schema preview for API consumers. If you return a `Map<String, Object>` or `String`, Swagger displays a generic, unhelpful `"{}"`.

##### D. Consistency with Spring Boot Framework Errors
When Spring Boot itself rejects a request (e.g. 404 for a wrong URL or 405 Method Not Allowed), Spring Boot's internal `DefaultErrorAttributes` produces:
```json
{
  "timestamp": "2026-11-18T10:00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/unknown"
}
```
By shaping `ErrorResponse` to match, our custom business exceptions blend seamlessly with the framework's native error responses.

##### E. Project Requirement Compliance
The project specification document (`Job Search System.docx`) explicitly mandated standard error envelopes containing `timestamp`, `status`, `error`, `message`, and `path`.

---

### Doubt 8: Deep Dive into Exception Handling: `@RestControllerAdvice`, `@Slf4j`, `HttpServletRequest`, `ResponseEntity`, and the Complete Lifecycle

#### 1. The Big Picture: How Does the Flow Work from Start to Finish?
Imagine a client calls `GET /api/employers/999` (an ID that does not exist in MySQL):
1. **Client Request:** Postman sends an HTTP GET request to port 8082.
2. **Tomcat Servlet Container:** Tomcat packages all incoming HTTP details (URL, headers, IP) into an object called `HttpServletRequest`.
3. **Controller Invocation:** Spring routes to `EmployerController.getEmployerById(999)`, which calls `employerService.getEmployerById(999)`.
4. **Exception Thrown:** In `EmployerServiceImpl`:
   ```java
   employerRepository.findById(999)
       .orElseThrow(() -> new EmployerNotFoundException(999));
   ```
   Because ID 999 is missing in MySQL, an `EmployerNotFoundException` is thrown!
5. **No `try-catch` in Controller:** We intentionally do NOT write `try-catch` inside our controllers or services. The exception bubbles upward.
6. **Spring Boot Intercepts:** Spring catches the exception and checks its application context:
   * *"Is there any class marked with `@RestControllerAdvice`?"* $\rightarrow$ YES, `GlobalExceptionHandler`!
   * *"Does it have an `@ExceptionHandler` matching `EmployerNotFoundException.class`?"* $\rightarrow$ YES, `handleEmployerNotFound`!
7. **Spring Calls the Handler:** Spring automatically executes:
   ```java
   handleEmployerNotFound(ex, request);
   ```
   and injects `ex` (the thrown exception) and `request` (the active `HttpServletRequest`).
8. **HTTP Response Returned:** The method constructs `ErrorResponse` and returns `new ResponseEntity<>(error, HttpStatus.NOT_FOUND)`.
9. **Network Response:** Spring writes the HTTP 404 status code and serializes `error` into JSON, sending it back to Postman/browser.

#### 2. What is `@RestControllerAdvice`?
* It combines `@ControllerAdvice` + `@ResponseBody`.
* Think of it as a **Global Safety Net** or **Global Interceptor** for all controllers in the application.
* **Why it matters:** Instead of repeating `try-catch` in every controller method, you centralize all error translation in this single class.

#### 3. What is `@Slf4j` and `log.warn(...)`?
* `@Slf4j` is a Lombok annotation that generates a logger instance behind the scenes:
  ```java
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  ```
* In basic Java, people use `System.out.println()`. In Spring Boot, `log.warn(...)` prints structured messages to your console with timestamps, thread names, and `WARN` severity level.
* It is purely for server terminal visibility so a developer watching the console knows an error happened.

#### 4. What is `HttpServletRequest` and why is it passed?
* `HttpServletRequest` is a built-in Java Servlet interface (`jakarta.servlet.http.HttpServletRequest`).
* It represents the raw incoming HTTP request from the client.
* **Why pass it here?** Strictly for ONE method call:
  ```java
  request.getRequestURI() // Returns "/api/employers/999"
  ```
* It allows `ErrorResponse` to tell the client: *"This error occurred on requested path: `/api/employers/999`"*.

#### 5. What is `ResponseEntity`? Did we define it?
* **It is built into Spring Framework** (`org.springframework.http.ResponseEntity`). We did NOT define it.
* A standard Java method can only return one object (the body).
* In HTTP, a real response has:
  1. **Status Code:** e.g., 200 OK, 201 Created, 404 Not Found.
  2. **Headers:** e.g., `Content-Type: application/json`.
  3. **Body:** The data or JSON payload.
* `ResponseEntity` is Spring's wrapper that lets you return BOTH the **Body** (`ErrorResponse`) AND the **HTTP Status Code** (`HttpStatus.NOT_FOUND`) together!

#### 6. Deconstructing: `return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);`
* **`new`:** Standard Java keyword to instantiate an object of the `ResponseEntity` class.
* **The Diamond `<>`:** Java Generics type inference. The method returns `ResponseEntity<ErrorResponse>`, so `<>` infers `<ErrorResponse>` without having to type it twice.
* **`(error, HttpStatus.NOT_FOUND)`:**
  * First parameter (`error`): Becomes the HTTP Response Body (converted to JSON).
  * Second parameter (`HttpStatus.NOT_FOUND`): Sets the HTTP Status header to 404.
* **Where does it get passed?** Spring Boot writes HTTP 404 to the network response, serializes `error` to JSON bytes, and sends it directly back to the calling client (Postman/browser).

---

### Doubt 9: Where does `MethodArgumentNotValidException` come from? Why is it not in `EmployerServiceImpl`? And what happens without `handleGenericException`?

#### 1. What is `MethodArgumentNotValidException` and who throws it?
* **Did we create it?** NO! It is a built-in exception provided directly by Spring Boot & Jakarta Validation (`org.springframework.web.bind.MethodArgumentNotValidException`).
* **Why isn't it in `EmployerServiceImpl`?**
  * Because it is thrown **before** the request ever reaches `EmployerServiceImpl`!
  * In `EmployerController.java`:
    ```java
    @PostMapping
    public ResponseEntity<EmployerResponseDTO> registerEmployer(
        @Valid @RequestBody EmployerRequestDTO requestDTO)
    ```
  * In `EmployerRequestDTO.java`:
    ```java
    @NotBlank(message = "Organization name cannot be blank")
    private String organizationName;

    @Email(message = "Email must be a valid email format")
    private String email;
    ```
  * When a user sends an empty company name or an invalid email address:
    1. Spring's validation interceptor sees the `@Valid` annotation.
    2. It inspects the incoming JSON against the `@NotBlank` and `@Email` rules.
    3. It detects that the JSON is invalid!
    4. **Spring Boot itself immediately throws `MethodArgumentNotValidException`** right at the controller boundary!
    5. The request is rejected before a single line of `EmployerServiceImpl` is executed.

#### 2. What does `handleValidationErrors` do with it?
```java
String validationErrors = ex.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining("; "));
```
* `ex.getBindingResult().getFieldErrors()`: Spring gives us a list of every validation rule that failed.
* It grabs the exact custom messages we wrote in `EmployerRequestDTO` (e.g. `"Organization name cannot be blank"`) and joins them with `"; "` into a clean string.
* It returns HTTP `400 Bad Request` so the frontend knows exactly which input field needs fixing.

#### 3. What is `handleGenericException` (`Exception.class`) and where is it called?
* In Java, `java.lang.Exception` is the universal superclass of all exceptions.
* It acts as the **Ultimate Safety Net** when an unexpected disaster occurs that we did not anticipate or write a specific handler for.
* **Examples of when it gets triggered:**
  * MySQL database crashes or loses network connection (`CannotCreateTransactionException`).
  * A `NullPointerException` occurs in business logic.
  * Out of memory or disk I/O failure.

#### 4. What happens if we DO NOT write `handleGenericException`?
If an unexpected crash occurs and we do not have a generic exception handler:
1. The error crashes unhandled all the way out of Spring.
2. Spring Boot falls back to its default internal error controller.
3. It displays the notorious **"Whitelabel Error Page"** (raw HTML) or leaks a 100-line Java stack trace showing database passwords, class names, and internal file paths to the user!
4. **Why this is bad:**
   * **Security Risk:** Leaking stack traces to the public internet is a major security vulnerability (Information Disclosure).
   * **Frontend Crash:** Frontend apps (React, mobile) expect a JSON response. Receiving an HTML Whitelabel page crashes the frontend parser.
5. With `handleGenericException`, any unexpected server crash is caught and returned as clean, safe JSON:
   ```json
   {
     "status": 500,
     "error": "Internal Server Error",
     "message": "An unexpected error occurred. Please contact system administrator."
   }
   ```

---

### Doubt 10: If I remove `MethodArgumentNotValidException`, will `handleGenericException` handle it?

#### Short Answer:
**YES, technically it will catch it.** Because in Java, `MethodArgumentNotValidException` is a subclass of `java.lang.Exception`.

#### But here is why that causes a major problem:
1. **Wrong HTTP Status Code (500 instead of 400):**
   * When a user forgets to type their company name or enters a 3-digit phone number, that is a **client-side input mistake**. It must return **HTTP 400 (Bad Request)**.
   * `handleGenericException` returns **HTTP 500 (Internal Server Error)**. An HTTP 500 tells the client and browser that your backend application crashed with a server bug!
2. **Useless / Missing Error Message:**
   * With `handleValidationErrors`: The client gets an exact, friendly message:
     ```json
     "message": "Organization name cannot be blank"
     ```
   * With `handleGenericException`: The client gets:
     ```json
     "message": "An unexpected error occurred. Please contact system administrator."
     ```
   * The user will have **no idea** why the registration failed, thinking the platform is broken instead of realizing they just missed filling a form field!

---

### Doubt 11: What is `@Transactional` in `EmployerServiceImpl`? And what does `readOnly = true` do?

#### 1. What is `@Transactional`? (The "All-or-Nothing" Safety Net)
In databases, a **Transaction** follows the ACID principle: either **ALL** database operations succeed, or if anything crashes halfway, **EVERYTHING is rolled back** (undone) as if it never happened!

##### The Real-World Disaster It Prevents:
Imagine a flow where two things happen in sequence:
* Step 1: Save a candidate's job application to the database.
* Step 2: Deduct 1 application credit from the candidate's account.

Now imagine Step 2 crashes with a database error!
* **Without `@Transactional`:** Step 1 was already committed! The application is saved, but the candidate was never charged a credit. Data is now inconsistent.
* **With `@Transactional`:** Spring wraps the method in a database transaction:
  * When the method begins: Spring executes `START TRANSACTION;` in MySQL.
  * If the method finishes cleanly: Spring executes `COMMIT;` (saves permanently).
  * If ANY runtime exception is thrown: Spring executes `ROLLBACK;` (undoes Step 1 completely so your database is never left with corrupt, half-finished data).

#### 2. What does `@Transactional(readOnly = true)` do?
Notice we use `readOnly = true` on `getEmployerById`, `getAllEmployers`, and `searchByOrganizationName`.

In these methods, we are **only reading** (`SELECT`), never inserting, updating, or deleting.

Telling Spring and Hibernate `readOnly = true` activates **3 major performance optimizations**:
1. **Disables Hibernate "Dirty Checking" (Major Memory & CPU Save):**
   * By default, whenever Hibernate fetches an entity, it keeps an extra copy ("snapshot") in memory. When the method ends, Hibernate compares the entity with the snapshot to see if any field was changed (dirty checking).
   * With `readOnly = true`, Hibernate knows you won't change anything. It **skips taking snapshots and skips dirty checking completely**, saving RAM and CPU cycles!
2. **Database-Level Optimization:**
   * MySQL knows this connection is read-only, so it avoids acquiring write locks and avoids setting up transaction rollback undo logs.
3. **Safety Guardrail:**
   * If a developer accidentally writes code that modifies an entity inside a `readOnly = true` method, Hibernate will refuse to flush changes to the database, protecting read operations from accidental side-effects.

---

## 8. Final Agreed Blueprint for Step 2A

### Finalized API Menu (6 Endpoints):
1. `POST /api/employers` — Register new company profile (`201 Created`).
2. `GET /api/employers/{id}` — Lookup company profile by ID (`200 OK`).
3. `GET /api/employers` — Get all employers with optional `city` and `industryType` filters (`200 OK`).
4. `GET /api/employers/search?name=...` — Search employers by organization name (`200 OK`).
5. `PUT /api/employers/{id}` — Update company profile with smart email check (`200 OK`).
6. `DELETE /api/employers/{id}` — Delete company profile (`200 OK`).
*(API 7 `/exists` is DISCARDED per user decision)*.

### Finalized Exception Strategy:
1. `EmployerNotFoundException` (HTTP 404 Not Found) — ID does not exist.
2. `DuplicateEmployerException` (HTTP 409 Conflict) — Email already registered.
3. `MethodArgumentNotValidException` (HTTP 400 Bad Request) — Bean Validation failures.
4. `CannotDeleteEmployerWithActiveJobsException` (HTTP 409 Conflict) — Created as an architectural template class (unwired until Step 3).
*(Exception 4 `EmployerAccountDeactivatedException` DISCARDED for simplicity)*.

