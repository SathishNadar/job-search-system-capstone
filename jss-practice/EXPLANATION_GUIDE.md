# Evaluation & Explanation Guide: Employer Microservice

Use this guide to prepare for any viva, code review, or demo evaluation. It explains every design choice, annotation, and layer in simple, professional terms.

---

## 1. High-Level Explanation (The 60-Second Elevator Pitch)

> *"For this task, I implemented the **Employer Microservice** from the Job Search System specification. It is a fully decoupled, self-contained RESTful service following industry-standard layered architecture: Controller, Service, Repository, and Database layers. It allows employers to register, manage company profiles, and search for candidates or organizations by industry and location.*
>
> *I enforced strict **Bean Validation** on input payloads, implemented **Global Exception Handling** using `@RestControllerAdvice` to generate standard error responses, documented all endpoints using **Springdoc OpenAPI (Swagger)**, configured **H2 in-memory DB** with PostgreSQL readiness, and wrote unit test suites using **JUnit 5 and Mockito** covering positive, negative, and validation scenarios."*

---

## 2. Why Each Layer Exists

| Layer | File / Class | Purpose | Why It Matters |
| :--- | :--- | :--- | :--- |
| **Controller** | `EmployerController.java` | Exposes REST endpoints (`POST`, `GET`, `PUT`, `DELETE`). | Handles HTTP requests, triggers validation via `@Valid`, returns standard HTTP status codes (`201`, `200`, `400`, `404`). |
| **DTO** | `EmployerRequestDTO.java`<br>`EmployerResponseDTO.java` | Data Transfer Objects. | **Decouples the database schema from the API contract**. Prevents over-posting, protects internal fields (like database IDs and timestamps), and houses validation annotations. |
| **Service** | `EmployerService.java`<br>`EmployerServiceImpl.java` | Business Logic Layer. | Contains business rules (e.g., duplicate email checks, data transformation between Entity and DTO, not-found checks). Marked with `@Transactional`. |
| **Repository** | `EmployerRepository.java` | Data Access Layer (Spring Data JPA). | Extends `JpaRepository<Employer, Long>`. Provides built-in CRUD operations and custom query derivation (`findByCityIgnoreCase`, `existsByEmail`). |
| **Entity** | `Employer.java` | Database Entity (JPA/Hibernate). | Maps directly to the `employers` table with all 13 attributes mandated by the case study document. Uses `@PrePersist` for auto-populating timestamps. |
| **Exceptions** | `GlobalExceptionHandler.java`<br>`EmployerNotFoundException.java`<br>`ErrorResponse.java` | Centralized Exception Handling. | Catches application exceptions before they crash the response, returning a clean, uniform JSON error object (`timestamp`, `status`, `error`, `message`, `path`). |

---

## 3. Key Annotations Cheatsheet & How to Explain Them

* **`@RestController`**: Combines `@Controller` and `@ResponseBody`. Tells Spring that every method return value is automatically serialized into JSON and written directly to the HTTP response body.
* **`@RequestMapping("/api/employers")`**: Sets the base URL path for all endpoints in this controller.
* **`@Valid`**: Tells Spring Boot to invoke the Bean Validation framework (Hibernate Validator) on the incoming `@RequestBody` object. If any rule (`@NotBlank`, `@Email`, etc.) is violated, it throws `MethodArgumentNotValidException`.
* **`@RestControllerAdvice`**: Global interceptor across all controllers. Any exception thrown by any controller method is captured here instead of sending default 500 error pages to the client.
* **`@ExceptionHandler(...)`**: Maps specific exception classes (e.g., `EmployerNotFoundException`, `MethodArgumentNotValidException`) to custom handler methods returning appropriate HTTP statuses (404, 400, 409).
* **`@Transactional`**: Manages database transactions automatically (commits on success, rolls back on runtime exceptions).
* **`@ExtendWith(MockitoExtension.class)`**: Integrates Mockito with JUnit 5, enabling `@Mock` and `@InjectMocks` without initializing a full Spring ApplicationContext (making tests lightning-fast).
* **`@WebMvcTest(EmployerController.class)`**: Slices the Spring context to test only the Controller layer and security/web filters using `MockMvc`, mocking the service layer with `@MockBean`.

---

## 4. Common Questions & Winning Answers

### Q1: Why did you use DTOs instead of passing the `Employer` entity directly to the controller?
> **Answer**:
> *"Directly exposing JPA entities to the API layer is an anti-pattern. DTOs provide three major benefits:*
> 1. *Security: Protects internal entity attributes like auto-generated primary keys and audit fields from being modified by clients.*
> 2. *Decoupling: Changes to database table structures do not break external API consumers.*
> 3. *Validation: Input validation rules belong on the input contract (`RequestDTO`), not the persistence entity."*

### Q2: How did you implement the standard error response mandated by the project document?
> **Answer**:
> *"I created an `ErrorResponse` POJO containing `timestamp`, `status`, `error`, `message`, and `path`. Then, in `GlobalExceptionHandler` annotated with `@RestControllerAdvice`, I created `@ExceptionHandler` methods for `EmployerNotFoundException` (returns 404), `DuplicateEmployerException` (returns 409), and `MethodArgumentNotValidException` (returns 400). It extracts field-level validation messages and builds the exact response format specified in the document."*

### Q3: How will this microservice integrate with the rest of the Job Search System later?
> **Answer**:
> *"In future steps:
> 1. It will register with the **Eureka Discovery Server** using `spring-cloud-starter-netflix-eureka-client`.
> 2. The **Spring Cloud Gateway** will route external traffic to this service via `/api/employers/**`.
> 3. Other services like the **Job Service** or **Job Application Service** will call this service using **Spring Cloud OpenFeign** to fetch employer verification details or company profiles."*

### Q4: How is data persisted right now?
> **Answer**:
> *"It currently uses an in-memory **H2 database** with `spring.jpa.hibernate.ddl-auto=update` and a `DataInitializer` bean that pre-seeds sample employers (like Capgemini and Google) on startup. When we deploy to production, we can simply uncomment the PostgreSQL datasource configuration in `application.properties` with zero code changes."*
