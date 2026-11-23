# Step 6: Authentication Microservice (`auth-service`) — Architecture & Implementation Guide (Gate 1)

> [!NOTE]
> For the complete theoretical breakdown of Spring Security 6, Stateless JWT vs. Stateful Sessions, BCrypt hashing, `OncePerRequestFilter`, and `SecurityContextHolder`, refer to [STEP_6_AUTH_SERVICE_THEORY.md](file:///c:/Users/satrajes/OneDrive%20-%20Capgemini/Documents/SpringProject/jss-capstone/STEP_6_AUTH_SERVICE_THEORY.md).

---

## 1. Overview & Architectural Role

The **`auth-service`** (running on **Port 8081**) serves as the **Central Identity Provider (IdP) & Token Issuer** for the **Job Search System (JSS)**.

Key Responsibilities:
1. **User Registration & Identity Storage:** Persists user credentials in dedicated MySQL schema `auth_db`, enforcing unique usernames/emails and irreversible **BCrypt** password hashing.
2. **Authentication & JWT Issuance:** Validates credentials on login via Spring Security's `AuthenticationManager` and issues signed **HMAC-SHA256 JSON Web Tokens (JWT)** carrying `userId`, `username`, and `role` claims.
3. **Stateless Token Verification:** Protects its own sensitive endpoints via `JwtAuthenticationFilter` (`OncePerRequestFilter`) and exposes token verification to support **Step 7 (`api-gateway` - Port 8080)**.
4. **Role-Based Access Control (RBAC):** Differentiates permissions cleanly between **`EMPLOYER`** and **`JOB_SEEKER`** accounts.
5. **Uniform Error Handling:** Both REST exceptions and Spring Security rejections (`401 Unauthorized` / `403 Forbidden`) return our exact 5-field `ErrorResponse` JSON envelope.

---

## 2. Service Specifications

| Specification | Configuration Detail |
| :--- | :--- |
| **Artifact ID** | `auth-service` |
| **Port** | `8081` |
| **Database** | MySQL: `auth_db` (`localhost:3306`) |
| **Spring Security** | `spring-boot-starter-security` (Stateless `SecurityFilterChain`, `BCryptPasswordEncoder`, `@EnableMethodSecurity`) |
| **JWT Library** | `io.jsonwebtoken:jjwt` (`0.12.6` — HS256 signed tokens, 24-hour expiration) |
| **Service Discovery** | Eureka Client registered as **`AUTH-SERVICE`** (Port 8761) |
| **API Documentation** | Springdoc OpenAPI 3.0 + Swagger UI with **`JWT AUTHORIZATION` (Bearer JWT)** button |
| **Lombok** | `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@RequiredArgsConstructor` |

---

## 3. Package & Module Structure

```text
jss-capstone/auth-service/
├── pom.xml                                        <-- Security + JJWT + JPA + MySQL + Eureka + OpenAPI
└── src/main/
    ├── java/com/jss/auth/
    │   ├── AuthServiceApplication.java            <-- @SpringBootApplication
    │   ├── config/
    │   │   ├── SecurityConfig.java                <-- SecurityFilterChain, BCrypt, AuthenticationManager, CORS/CSRF
    │   │   ├── OpenApiConfig.java                 <-- Configures Swagger Bearer JWT SecurityScheme
    │   │   └── DataInitializer.java               <-- Seeds 2 test accounts (1 EMPLOYER, 1 JOB_SEEKER) with BCrypt
    │   ├── security/
    │   │   ├── JwtUtil.java                       <-- Generates, parses & validates HS256 JWT tokens
    │   │   ├── CustomUserDetailsService.java      <-- Loads User from MySQL -> GrantedAuthority("ROLE_<role>")
    │   │   ├── JwtAuthenticationFilter.java       <-- OncePerRequestFilter: verifies Authorization: Bearer <token>
    │   │   ├── JwtAuthenticationEntryPoint.java   <-- Clean 401 JSON when unauthenticated / invalid token
    │   │   └── CustomAccessDeniedHandler.java     <-- Clean 403 JSON when role lacks permission
    │   ├── controller/
    │   │   └── AuthController.java                <-- Public & Protected Auth endpoints
    │   ├── dto/
    │   │   ├── RegisterRequestDTO.java
    │   │   ├── LoginRequestDTO.java
    │   │   ├── AuthResponseDTO.java
    │   │   ├── UserResponseDTO.java
    │   │   └── TokenValidationResponseDTO.java
    │   ├── entity/
    │   │   └── User.java                          <-- 7 fields (trimmed status per user alignment)
    │   ├── exception/
    │   │   ├── DuplicateUserException.java        <-- 409 Conflict
    │   │   ├── UnauthorizedException.java         <-- 401 Unauthorized
    │   │   ├── UserNotFoundException.java         <-- 404 Not Found
    │   │   ├── ErrorResponse.java                 <-- Standard 5-field envelope
    │   │   └── GlobalExceptionHandler.java        <-- @RestControllerAdvice
    │   ├── repository/
    │   │   └── UserRepository.java
    │   └── service/
    │       ├── AuthService.java
    │       └── impl/AuthServiceImpl.java
    └── resources/
        └── application.properties                 <-- Port 8081, auth_db, jwt.secret, jwt.expiration
```

---

## 4. Entity Model: `User.java` (7 Fields)

Mapped to table `users` in `auth_db` *(the docx's `status` column was removed per user alignment since no deactivate/status API is used)*:

| Field Name | Java Type | Constraint / Default | Purpose |
| :--- | :--- | :--- | :--- |
| `userId` | `Long` | `@Id`, `@GeneratedValue(IDENTITY)` | Primary Key |
| `username` | `String` | `nullable = false, unique = true`, length = 50 | Unique login handle (e.g. `"capgemini_hr"`) |
| `password` | `String` | `nullable = false`, length = 255 | Irreversible 60-char **BCrypt hash** (`$2a$10$...`) |
| `email` | `String` | `nullable = false, unique = true`, length = 100 | Unique email address (can also log in with email!) |
| `role` | `String` | `nullable = false`, length = 20 | Strictly `"EMPLOYER"` or `"JOB_SEEKER"` |
| `createdDate` | `LocalDateTime` | `nullable = false, updatable = false` | Auto-populated in `@PrePersist` |
| `modifiedDate` | `LocalDateTime` | `nullable = false` | Auto-populated in `@PrePersist` & updated in `@PreUpdate` |

---

## 5. Candidate REST API Menu (Gate 1 Review)

### Tier 1: Public / Non-Protected Routes (`permitAll()`)

| # | Method | Endpoint URI | Request Body / Params | Response Body | Status | Business Scenario |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **`POST`** | `/api/auth/register` | `RegisterRequestDTO` | `AuthResponseDTO` | `201 Created` | **Register & Auto-Login:** Validates uniqueness (`username` & `email`), BCrypt-hashes password, saves user (`EMPLOYER` or `JOB_SEEKER`), and immediately returns signed JWT (Option B). |
| **2** | **`POST`** | `/api/auth/login` | `LoginRequestDTO` | `AuthResponseDTO` | `200 OK` | **Authenticate & Issue JWT:** Accepts `usernameOrEmail` + raw password. Verifies BCrypt hash via `AuthenticationManager` and issues signed JWT. |
| **3** | **`GET`** | `/api/auth/validate` | `@RequestHeader("Authorization")` or `?token=` | `TokenValidationResponseDTO` | `200 OK` / `401` | **Verify Token:** Inspects a JWT signature & expiry and returns `{ valid, userId, username, role }`. Useful for Swagger debugging & Gateway checks. |

### Tier 2: Protected Routes (`Bearer <token>` Required!)

| # | Method | Endpoint URI | Access Rule | Response Body | Status | Business Scenario |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| **4** | **`GET`** | `/api/auth/me` | Any Authenticated User | `UserResponseDTO` | `200 OK` / `401` | **My Profile:** Extracts logged-in username directly from `SecurityContextHolder` (JWT) and returns account metadata (no password!). |

*(Note: Optional `/employer/check` and `/deactivate` endpoints were dropped per user alignment. Old-password `/change-password` was moved to **Future Scope**—to be implemented via Email OTP / Verification Link reset flow instead).*

> [!TIP]
> **Future Scope:** Password Reset / Forgot Password via Email OTP or Verification Link (using JavaMailSender / notification integration) rather than submitting the old password.

---

## 6. DTO Specifications (Lombok + Bean Validation + OpenAPI `@Schema`)

### 1. `RegisterRequestDTO`
* `username`: `@NotBlank`, `@Size(min = 3, max = 50)`
* `email`: `@NotBlank`, `@Email`
* `password`: `@NotBlank`, `@Size(min = 6, max = 100, message = "Password must be at least 6 characters")`
* `role`: `@NotBlank`, `@Pattern(regexp = "^(EMPLOYER|JOB_SEEKER)$", message = "Role must be either 'EMPLOYER' or 'JOB_SEEKER'")`

### 2. `LoginRequestDTO`
* `usernameOrEmail`: `@NotBlank(message = "Username or email is required")`
* `password`: `@NotBlank(message = "Password is required")`

### 3. `AuthResponseDTO`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInMs": 86400000,
  "userId": 1,
  "username": "capgemini_hr",
  "email": "careers@capgemini.com",
  "role": "EMPLOYER"
}
```

### 4. `UserResponseDTO` *(Never exposes `password`!)*
* `userId`, `username`, `email`, `role`, `createdDate`, `modifiedDate`

### 5. `TokenValidationResponseDTO`
* `valid` (`boolean`), `userId`, `username`, `role`, `message`

---

## 7. Exception & Security Rejection Architecture (Uniform 5-Field `ErrorResponse`)

In Spring Security 6, errors happen in **two separate places**—and `@RestControllerAdvice` alone cannot catch Filter-layer rejections! Both layers are covered below:

### Layer A: Controller / Service Layer (`GlobalExceptionHandler` via `@RestControllerAdvice`)

| Exception | HTTP Status | Trigger Condition | Example Message |
| :--- | :--- | :--- | :--- |
| `DuplicateUserException` | `409 Conflict` | `username` or `email` already registered during `/register` | `"Username 'capgemini_hr' is already taken"` |
| `UnauthorizedException` / `BadCredentialsException` | `401 Unauthorized` | Wrong password/username on `/login`, or invalid/expired token on `/validate` (Choice A) | `"Invalid username/email or password"` / `"Invalid or expired JWT token"` |
| `UserNotFoundException` | `404 Not Found` | Authenticated username from JWT no longer exists in `auth_db` | `"User not found"` |
| `MethodArgumentNotValidException` | `400 Bad Request` | Bean Validation errors (`@NotBlank`, `@Email`, `@Size`, `@Pattern`) | Joined field messages separated by `"; "` |

### Layer B: Security Filter Layer (`SecurityConfig` — Runs BEFORE Controller!)

| Security Handler Class | HTTP Status | Trigger Condition | Example Message |
| :--- | :--- | :--- | :--- |
| `JwtAuthenticationEntryPoint` (`AuthenticationEntryPoint`) | `401 Unauthorized` | Missing `Authorization` header, missing `"Bearer "`, expired, or tampered JWT on protected URL (`/me`) | `"Authentication required: missing, expired, or invalid Bearer token"` |
| `CustomAccessDeniedHandler` (`AccessDeniedHandler`) | `403 Forbidden` | Valid JWT rejected by `SecurityFilterChain` URL role rules (also serves Step 7 Gateway / future RBAC) | `"Access Denied: You do not have permission to access this resource"` |

---

## 8. Gate 1 Alignment Checklist

1. **API Endpoints Scope (4 Core Endpoints Locked In):**
   * **Public (3):** `POST /api/auth/register` (`AuthResponseDTO` — Option B), `POST /api/auth/login`, `GET /api/auth/validate` (Choice A `401` on invalid token).
   * **Protected (1):** `GET /api/auth/me`.
   * *(`#5 /change-password` moved to Email OTP Future Scope; `#6 /employer/check` and `#7 /deactivate` dropped).*
2. **Login Identifier (`username + email` Locked In):**
   * Supports logging in via `usernameOrEmail` using `findByUsernameOrEmail(input, input)`.
3. **Registration Return Type (Option B Locked In):**
   * `POST /api/auth/register` returns `AuthResponseDTO` with instant JWT.
4. **Entity & Exceptions Locked In:**
   * `User` streamlined to **7 fields** (`status` removed).
   * Lean exceptions (`DuplicateUserException`, `UnauthorizedException`, `UserNotFoundException`, `BadCredentialsException`, `MethodArgumentNotValidException` + `JwtAuthenticationEntryPoint` & `CustomAccessDeniedHandler`).
   * **Gate 1 Complete — Ready for user `"proceed"` to start Gate 2 implementation!**

---

## 9. Doubt Solving & Conceptual Deep-Dives

### Doubt 1: What are `SecurityFilterChain` and `@EnableMethodSecurity` in Spring Security 6?
1. **`SecurityFilterChain` (URL-Level Security Pipeline):**
   * Replaces the old `WebSecurityConfigurerAdapter` (removed in Spring Security 6).
   * Declared as a `@Bean` inside `SecurityConfig.java`. It is the master pipeline of filters every incoming HTTP request walks through **before reaching any `@RestController`**:
     * Disables CSRF (stateless REST APIs don't use browser cookie sessions).
     * Sets `SessionCreationPolicy.STATELESS` (no server RAM sessions).
     * Defines broad URL access rules (`permitAll()` for `/register`, `/login`, `/validate` vs `.authenticated()` for everything else).
     * Plugs `JwtAuthenticationFilter` into the chain before `UsernamePasswordAuthenticationFilter`.
     * Wires `JwtAuthenticationEntryPoint` (`401`) and `CustomAccessDeniedHandler` (`403`).
2. **`@EnableMethodSecurity` (Annotation-Driven Method Security):**
   * Placed on `SecurityConfig.java`. Enables `@PreAuthorize(...)` and `@PostAuthorize(...)` directly on individual Java controller/service methods.
   * While `SecurityFilterChain` guards broad URL routes (*"Must be authenticated to enter `/api/auth/**`"*), `@EnableMethodSecurity` adds surgical role checks per method (*"`@PreAuthorize("hasRole('EMPLOYER')")`"*). Without it, Spring silently ignores `@PreAuthorize`.

---

### Doubt 2: How does JJWT (`io.jsonwebtoken`) generate and sign tokens?
* JJWT takes your Header (`HS256`) + Payload (`userId`, `sub`, `role`, `iat`, `exp`) and computes:
  $$\text{Signature} = \text{HMACSHA256}\big(\text{Base64Url}(\text{Header}) + \text{"."} + \text{Base64Url}(\text{Payload}),\; \text{SECRET\_KEY}\big)$$
* It packages all three parts into the compact string `header.payload.signature` with your configured expiration timestamp (`jwt.expiration`). On subsequent requests, JJWT parses the token, re-verifies the HMAC-SHA256 signature, checks expiration, and extracts claims.

---

### Doubt 3: Does `/login` automatically call `/validate`?
* **No!**
  * `POST /api/auth/login` verifies your raw password against the MySQL BCrypt hash via `AuthenticationManager` and **generates (creates)** a new JWT using `JwtUtil.generateToken(user)`. It does not call `/validate`.
  * `GET /api/auth/validate` is an independent endpoint used *after* someone already has a token and wants to check whether it is valid/unexpired and inspect its claims (`userId`, `username`, `role`).

---

### Doubt 4: What is `GET /api/auth/me` used for in a real frontend?
* In a React/Angular app, refreshing the page (`F5`) wipes JS in-memory user state, leaving only the saved JWT string.
* On startup/refresh, the frontend automatically sends `GET /api/auth/me` with `Authorization: Bearer <token>`.
* `auth-service` reads the verified username from `SecurityContextHolder`, loads their latest database row (`userId`, `username`, `email`, `role`, `status`, `createdDate`, `modifiedDate` — never password!), and returns it.
* **Security benefit:** No `{id}` parameter appears in the URL—preventing users from modifying `/users/5` to `/users/6` to read someone else's account.

---

### Doubt 5: Where would `GET /api/auth/employer/check` (#6) be used vs. testing RBAC in Step 7?
* In a finished portal, `/api/auth/employer/check` is **not** needed by the frontend—real role checks happen on actual business APIs (`POST /api/jobs` for `EMPLOYER`, `POST /api/applications` for `JOB_SEEKER`) enforced at **`api-gateway` (Step 7, Port 8080)**.
* It was proposed solely as a **standalone Step 6 test endpoint** to let you observe `403 Forbidden` directly on port `8081` before building the Gateway. If you want zero dummy endpoints in `auth-service`, it can be dropped cleanly.

---

### Doubt 6: How does `PATCH /api/auth/deactivate` (`status = INACTIVE`) work, what happens on login, and can an account be reactivated?
1. **Isolated to `auth_db`:** `status` lives in `auth_db.users`—completely independent of `employer_db` / `jobseeker_db`. Deactivating blocks authentication across JSS while preserving historical jobs, applications, and messages.
2. **What happens if an `INACTIVE` user enters the CORRECT email and password at `/login`?**
   * `CustomUserDetailsService` builds Spring's `UserDetails` with `enabled = "ACTIVE".equalsIgnoreCase(user.getStatus())` (`false`).
   * Even though the BCrypt password matches, `AuthenticationManager` throws **`DisabledException`**, caught by `GlobalExceptionHandler` $\rightarrow$ returns **`403 Forbidden`**: `"Account is INACTIVE. Login denied."` (No JWT is issued!).
   * `JwtAuthenticationFilter` also respects `userDetails.isEnabled()` so even previously issued tokens stop working if an account becomes `INACTIVE`.
3. **How can an account be made `ACTIVE` again?**
   * Because a deactivated user cannot log in to obtain a Bearer token, reactivation cannot sit behind a JWT-protected endpoint alone.
   * Standard approaches:
     * **Option A (`POST /api/auth/reactivate` with `LoginRequestDTO`):** Verifies raw password against BCrypt; if correct, sets `status = "ACTIVE"`, updates `modifiedDate`, and allows login again (like Instagram/LinkedIn account reactivation).
     * **Option B (Admin / Support Reactivation):** Only an Admin flips `status` back to `"ACTIVE"`.

---

### Doubt 7: What does "Disables CSRF (cookie protection not needed for stateless JWT APIs)" mean?
* **What is CSRF (Cross-Site Request Forgery)?**
  If `bank.com` authenticates via browser **Cookies**, and you open a malicious Tab 2 (`evil.com`) containing a hidden auto-submitting `<form action="https://bank.com/transfer" method="POST">`, Chrome **automatically attaches your `bank.com` Cookie** to that request—making the bank think you approved the transfer!
* **Why Spring Security enables CSRF by default:** Spring assumes browser session cookies are used, so it blocks every `POST`/`PUT`/`DELETE` without an extra `X-CSRF-TOKEN` (`403 Forbidden`).
* **Why we call `.csrf(csrf -> csrf.disable())`:** Stateless REST APIs send JWTs inside the **`Authorization: Bearer <token>`** HTTP header—which browsers **never** auto-attach from third-party tabs! Thus CSRF is impossible against pure Bearer APIs, and disabling it lets Bruno, Postman, Swagger, and React call `POST` endpoints cleanly.

---

### Doubt 8: Does `GET /api/auth/validate` have a production use case or is it just for debugging?
* In our Step 7 `api-gateway` architecture, the Gateway shares `SECRET_KEY` and validates JWTs locally in RAM in microseconds (`0` network hops)—so the Gateway does not need to call `/api/auth/validate`.
* Thus, `/api/auth/validate` is purely a convenient **debugging/inspection helper** (or used only if a microservice without the secret key wanted remote validation via Feign).

---

### Doubt 9: If we used `GET /api/auth/users/{id}` instead of `/me` and User 5 changed `{id}` to `6`, wouldn't the JWT stop it?
* **NO—NOT automatically!** That exact security flaw is **OWASP API #1: IDOR / BOLA (Broken Object Level Authorization)**!
* `JwtAuthenticationFilter` only verifies: *"Does this JWT belong to ANY valid logged-in user (User 5)?"* It has **zero knowledge** of what number `{id}` you typed in the URL path!
* Unless the developer explicitly writes an extra manual guard inside the controller (`if (!jwtUserId.equals(pathId)) throw AccessDenied`), calling `GET /api/auth/users/6` with User 5's valid JWT **would leak User 6's record!**
* **`GET /api/auth/me` prevents IDOR 100% by design:** There is no `{id}` in the URL at all—the backend only looks up the identity extracted directly from the signed JWT!

---

### Doubt 10: Difference between `register` returning `UserResponseDTO` vs. `AuthResponseDTO` (JWT)?
* **Option A (`UserResponseDTO` — Classic 2-Step):** `/register` creates the account and returns profile info (no token). The user is redirected to Login (`/login`) to receive their JWT. Clean separation of responsibilities.
* **Option B (`AuthResponseDTO` — Instant Auto-Login):** `/register` saves the account AND immediately issues a JWT in the response so a frontend doesn't force the user to re-type their password right after signing up.

---

### Doubt 11: Where does `username` come from, and how many lines of code does `usernameOrEmail` login add?
1. **Where `username` lives:** `Job Search System.docx` defines a dedicated `User` entity inside `auth-service` (`auth_db.users`) with BOTH **`username`** (a login handle chosen during `/register`, e.g., `"sathish_dev"`, separate from `firstName`/`lastName` in `jobseeker_db` or `organizationName` in `employer_db`) and **`email`**.
2. **Complexity / Lines of code:** Zero complexity—**literally 1 JPA derived query method**!
   ```java
   // UserRepository.java (1 line!)
   Optional<User> findByUsernameOrEmail(String username, String email);
   ```
   Passing the same login string twice `findByUsernameOrEmail(input, input)` tells MySQL:
   `WHERE username = ? OR email = ?`. Whether the user enters `"sathish_dev"` or `"sathish@gmail.com"`, it works automatically!

---

### Doubt 12: If we don't store JWT in cookies, why do we still have to call `.csrf().disable()`?
* Because **Spring Security turns CSRF protection ON automatically by default**!
* If `.csrf(csrf -> csrf.disable())` is omitted, Spring's internal `CsrfFilter` intercepts every incoming `POST`, `PUT`, `PATCH`, or `DELETE` request (including `POST /api/auth/register` and `/login`) and blocks it with **`403 Forbidden`** expecting a server-generated HTML form `_csrf` token.
* Calling `.csrf().disable()` does not weaken security—it simply turns off a browser-cookie check that isn't needed for Bearer-header REST APIs.

---

### Doubt 13: Why use `Authorization: Bearer` headers instead of Cookies in microservices, and where is the token stored?
1. **Why REST Microservices prefer `Authorization: Bearer` over Cookies:**
   * **No CSRF Exposure:** Cookies trigger browser auto-attachment across tabs, forcing CSRF token complexity; Bearer headers are never auto-sent cross-site.
   * **Zero Cross-Origin / Localhost Port Headaches:** Microservices & SPAs run across multiple ports (`3000`, `8080`, `8081`); cookies require strict `SameSite=None; Secure` + HTTPS rules, whereas `Authorization: Bearer` works identically everywhere.
   * **Client-Agnostic:** Mobile apps (Android/iOS), OpenFeign inter-service calls, CLI tools, Postman/Bruno, and Swagger's `JWT AUTHORIZATION` UI natively speak Bearer headers without browser cookie jars.
2. **Where the JWT is stored:**
   * **On Backend:** **Nowhere** (stateless).
   * **On Client:** Stored by whichever client consumes the API—Swagger holds it in browser memory under `JWT AUTHORIZATION`, Bruno/Postman holds it in `{{token}}` env vars, and a React/Next.js frontend stores it in memory/storage (or wraps it inside its own BFF `HttpOnly` cookie).

---

### Doubt 14: Exact Code Change Between `/register` Returning `UserResponseDTO` (Option A) vs `AuthResponseDTO` (Option B)
* Moving from Option A to Option B adds **zero architectural complexity**—only **1 line** (`jwtUtil.generateToken(savedUser)`) in `AuthServiceImpl` and changing the return type from `UserResponseDTO` to `AuthResponseDTO` in `AuthController` (sharing a single `buildAuthResponse(User)` helper with `login()`).

---

### Doubt 15: Where did `status` (`ACTIVE`/`INACTIVE`) and `DisabledException` come from?
* **Honest Accountability Check:** You did **not** ask to build an active/inactive toggle API (you explicitly dropped `#7 PATCH /api/auth/deactivate`).
* **`status` came strictly from `Job Search System.docx`:** Section 3 (`Entity: User`) lists `status (String)` alongside `userId`, `username`, `password`, `email`, `role`, `createdDate`, and `modifiedDate`.
* Since no endpoint sets a user to `INACTIVE`, `DisabledException` would only ever trigger if someone manually edited a row to `'INACTIVE'` in MySQL Workbench.

---

### Doubt 16: What is the difference between `AccessDeniedException` and `CustomAccessDeniedHandler`?
Both handle the exact same event (**`403 Forbidden`**), at **two different places in Spring's request lifecycle**:
1. **Before entering any Controller (Filter Chain Level):**
   * If `SecurityConfig` blocks a URL (e.g. `.requestMatchers("/api/employer/**").hasRole("EMPLOYER")`), Spring's `AuthorizationFilter` rejects the call *before* `DispatcherServlet` reaches `GlobalExceptionHandler`.
   * Spring calls **`CustomAccessDeniedHandler`** (`AccessDeniedHandler` interface).
2. **Inside a Controller/Service Method (`@PreAuthorize` Method Security):**
   * When `@PreAuthorize("hasRole('EMPLOYER')")` fails inside an MVC controller proxy, Spring throws **`AccessDeniedException`** inside the MVC layer where **`GlobalExceptionHandler` (`@RestControllerAdvice`) intercepts it first**.
   * **The Catch-All Gotcha:** If `GlobalExceptionHandler` has `@ExceptionHandler(Exception.class)` (our 500 handler) WITHOUT an explicit `@ExceptionHandler(AccessDeniedException.class)` (or without rethrowing it), Spring swallows the `403` and returns **`500 Internal Server Error`**!
   * Because our remaining 4 endpoints (`register`, `login`, `validate`, `me`) do not use role-restricted `@PreAuthorize` rules yet, neither is needed unless you add a `@PreAuthorize` method later—keeping `CustomAccessDeniedHandler` cleanly wired in `SecurityConfig` is standard Spring Security hygiene.

