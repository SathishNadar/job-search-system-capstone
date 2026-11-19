# Step 1: Eureka Discovery Server (`eureka-server`) — Architectural & Educational Guide

> **Module Name:** `eureka-server`  
> **Assigned Port:** `8761` (Industry Standard Eureka Default)  
> **Status:** Gate 1 Plan & Documentation Phase (No code generated yet)

---

## 1. What This Step Implements & What It Brings to the Table

In a traditional monolithic application, everything runs in a single process. But in our **Job Search System**, we have **8 distinct microservices** running on different ports (`8081`, `8082`, `8084`, `8085`, etc.).

### The Problem in Microservices (Without Eureka):
* If `job-service` needs to check an employer, it would have to hardcode:
  ```java
  String url = "http://localhost:8082/api/employers/1"; // BAD PRACTICE!
  ```
* **Why hardcoding is dangerous:**
  1. What if you deploy `employer-service` to a cloud server or Docker where the IP changes dynamically?
  2. What if you run 3 copies (instances) of `employer-service` to handle heavy traffic? Which port do you hardcode?
  3. If `employer-service` crashes, the other services have no way of knowing and will keep sending traffic into a black hole!

### The Solution: Netflix Eureka Discovery Server (The Central Phonebook)
Eureka acts as a **dynamic, automated, self-healing registry**:

```
 ┌────────────────────────────────────────────────────────┐
 │           EUREKA DISCOVERY SERVER (Port 8761)          │
 │  Registry Phonebook:                                   │
 │  • "EMPLOYER-SERVICE"  ──> http://192.168.1.15:8082    │
 │  • "JOB-SERVICE"       ──> http://192.168.1.15:8084    │
 │  • "JOBSEEKER-SERVICE" ──> http://192.168.1.15:8083    │
 └───────────────────────────▲────────────────────────────┘
                             │
            ┌────────────────┴────────────────┐
            │ Heartbeat every 30s ("I am alive!")
            │
 ┌──────────┴────────────┐        ┌───────────┴───────────┐
 │   employer-service    │        │      job-service      │
 │      (Port 8082)      │        │      (Port 8084)      │
 └───────────────────────┘        └───────────────────────┘
```

1. **Service Registration:** When any microservice boots up, it automatically contacts Eureka: *"Hi, I am `EMPLOYER-SERVICE`, running at `localhost:8082`"*.
2. **Heartbeats & Self-Healing:** Every 30 seconds, the service pings Eureka: *"I am still healthy!"*. If Eureka doesn't hear from a service for 90 seconds, it automatically evicts it from the phonebook!
3. **Dynamic Discovery:** When `job-service` wants to talk to `employer-service`, it asks Eureka by name: *"Where is `EMPLOYER-SERVICE` right now?"*. Eureka returns the live address.

---

## 2. File & Folder Inventory: What Will Be Created

```
jss-capstone/
├── pom.xml                                     <-- Parent POM (we will register eureka-server module here)
└── eureka-server/                              <-- NEW SUB-MODULE FOLDER
    ├── pom.xml                                 <-- Child Maven build file
    └── src/
        └── main/
            ├── java/com/jss/eureka/
            │   └── EurekaServerApplication.java <-- Spring Boot entry point with @EnableEurekaServer
            └── resources/
                └── application.yml             <-- Eureka configuration settings
```

### Purpose of Each File:

| File | Purpose | Why It Exists |
| :--- | :--- | :--- |
| **`eureka-server/pom.xml`** | Child POM | Inherits from parent `jss-capstone`. Imports `spring-cloud-starter-netflix-eureka-server`. |
| **`EurekaServerApplication.java`** | Ignition Switch | Contains `main()` method and `@EnableEurekaServer` to activate the registry engine. |
| **`application.yml`** | Configuration | Sets port `8761` and disables Eureka from trying to register with itself. |

---

## 3. Important Concepts & Code Snippets Explained

### Concept 1: The `@EnableEurekaServer` Annotation

In `EurekaServerApplication.java`:
```java
package com.jss.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer // <--- THE MAGIC ANNOTATION!
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

#### What does `@EnableEurekaServer` do?
* It triggers Spring Cloud to start the **Eureka Server Controller and Registry Bean**.
* It starts the in-memory lookup table that stores service names and IP addresses.
* It hosts the visual **Eureka Web Dashboard** on port `8761`.

---

### Concept 2: The Self-Registration Problem (`application.yml`)

Inside `eureka-server/src/main/resources/application.yml`:
```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false   # <--- Do NOT register with yourself!
    fetch-registry: false          # <--- Do NOT fetch the registry from yourself!
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### Why do we set `register-with-eureka: false` and `fetch-registry: false`?
* By default, every Eureka dependency assumes it is a **client microservice** that needs to connect to an external Eureka server.
* If you do not set these to `false`, the Eureka server will repeatedly try to contact itself on startup, printing thousands of error logs: `"Cannot connect to server at localhost:8761"`.
* Telling it `false` says: *"You are the master registry server. Do not register as a client inside yourself."*

---

### Concept 3: How Client Microservices Connect (Future Preview)

When we build `employer-service` in Step 2, how does it talk to this Eureka server?

#### Step A: In client's `pom.xml`:
```xml
<!-- Client dependency for Eureka -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### Step B: In client's `application.properties`:
```properties
spring.application.name=employer-service
server.port=8082

# Tell the client where the Eureka phonebook lives:
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```
The moment `employer-service` boots up, it automatically registers under the name **`EMPLOYER-SERVICE`** on port `8082`!

---

### Concept 4: How OpenFeign Resolves Names (Future Preview)

When `job-service` needs to verify an employer, it writes an OpenFeign client referencing the name registered in Eureka:

```java
@FeignClient(name = "employer-service") // <--- Matches the name in Eureka!
public interface EmployerClient {
    @GetMapping("/api/employers/{id}")
    EmployerResponseDTO getEmployerById(@PathVariable("id") Long id);
}
```
* Notice there is **NO IP ADDRESS AND NO PORT NUMBER**!
* Spring Cloud automatically asks Eureka: *"Give me the live IP and port for `employer-service`"*.
* If `employer-service` moves from port `8082` to port `9090`, `job-service` still works with **zero code changes**!

---

## 4. Verification Plan (How We Will Know It Works)

Once we implement Step 1 and run the server:
1. Open your web browser and go to:
   👉 **`http://localhost:8761`**
2. You will see the **Spring Cloud Netflix Eureka Dashboard**:
   * **System Status:** Environment, Data center, Memory usage, Uptime.
   * **Instances currently registered with Eureka:** A table showing all connected services. *(Initially empty, and as we build Employer, Job, etc., they will show up here live!)*

---

## 5. Doubt Solving & Conceptual Deep-Dives

### Doubt 1: Why can't we assign Static / Elastic IPs in Cloud or Docker like we do in Monolithic systems?

#### The Monolith Intuition:
> *"In AWS, for my monolithic app, I assigned an Elastic IP (Static IP) to my EC2 instance and pointed DNS to it. Why can't we just get 8 static IPs for our 8 microservices?"*

#### Why That Fails in Microservices & Cloud:
1. **The Auto-Scaling Reality in Cloud (AWS / GCP / Azure):**
   * In a monolith, you run 1 or 2 permanent servers.
   * In microservices, services **scale dynamically based on load**.
   * *Example:* On Monday morning at 9:00 AM, thousands of candidates apply for jobs. AWS Auto-Scaling automatically spawns **5 brand new servers** for `job-service`. At 1:00 PM, traffic drops, and AWS **destroys** those 5 servers.
   * **The Catch:** AWS charges for Elastic IPs and limits accounts to only 5 per region. When auto-scalers spin up new servers, AWS assigns **ephemeral (temporary) private IPs** (e.g., `10.0.1.42`, `10.0.1.98`). You cannot assign manual static IPs to servers that only exist for 2 hours!
2. **The Docker / Containerization Reality (How IPs Change):**
   * When you run a Docker container:
     ```bash
     docker run -d employer-service
     ```
     Docker's internal bridge network gives it an IP, say: **`172.17.0.2`**.
   * When the container crashes, or you deploy an update (`docker restart` or `docker compose up`), **Docker destroys the old container and creates a new one**.
   * Docker's DHCP assigns the next available IP: **`172.17.0.5`**!
   * If other microservices had hardcoded `172.17.0.2`, they break immediately with `Connection Refused`!
   * **With Eureka:** The new container boots up, grabs whatever IP Docker gave it (`172.17.0.5`), and registers with Eureka under `EMPLOYER-SERVICE`. No hardcoded IPs needed!

---

### Doubt 2: How are multiple instances created, who creates them, and can they have the SAME name in Eureka?

#### A. Who creates multiple copies (instances) of a microservice?
* **In Production Cloud:** Container Orchestrators like **Kubernetes (HPA - Horizontal Pod Autoscaler)** or **AWS ECS** monitor CPU and memory. When CPU exceeds 75%, Kubernetes automatically launches a second and third instance of `employer-service`.
* **In Local Development:** YOU can create multiple copies on your laptop by running the same `.jar` on different ports in two terminal tabs:
  * Terminal 1: `java -jar employer-service.jar --server.port=8082`
  * Terminal 2: `java -jar employer-service.jar --server.port=8083`

#### B. Can all instances share the exact SAME name `"employer-service"` in Eureka?
**YES! That is the core superpower of Eureka.**
Every instance has the exact same configuration in `application.properties`:
```properties
spring.application.name=employer-service
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```
In the Eureka dashboard at `http://localhost:8761`, Eureka lists:

| Application Name | Registered Instances | Status |
| :--- | :--- | :--- |
| **`EMPLOYER-SERVICE`** | `192.168.1.15:8082`, `192.168.1.15:8083`, `192.168.1.15:8084` | **UP (3)** |

#### C. How does the client call them and balance the load?
When `job-service` calls `employer-service` using OpenFeign:
```java
@FeignClient(name = "employer-service")
public interface EmployerClient {
    @GetMapping("/api/employers/{id}")
    EmployerResponseDTO getEmployerById(@PathVariable("id") Long id);
}
```
**Spring Cloud LoadBalancer** automatically distributes requests between the 3 live instances using **Round-Robin**:
* Request 1 $\rightarrow$ Instance 1 (Port 8082)
* Request 2 $\rightarrow$ Instance 2 (Port 8083)
* Request 3 $\rightarrow$ Instance 3 (Port 8084)
* Request 4 $\rightarrow$ Instance 1 (Port 8082)

---

### Doubt 3: If `employer-service` crashes, what does "sending traffic into a black hole" mean?

#### Scenario WITHOUT Eureka (The Black Hole Disaster):
```
[ User on Web ] ──▶ [ job-service ] ──(Hardcoded: 10.0.1.25:8082)──▶ [ employer-service ] (CRASHED! 💥)
```
1. `employer-service` runs out of memory and crashes at 2:00 AM.
2. At 2:01 AM, a candidate applies for a job. `job-service` attempts to reach the hardcoded IP `10.0.1.25:8082`.
3. **What happens:**
   * The operating system socket waits for 30 to 60 seconds (TCP Timeout).
   * The user's screen is frozen on a loading spinner.
   * After 60 seconds, the user gets an error: `504 Gateway Timeout` or `Connection Refused`.
   * **Why it's a "Black Hole":** If 500 users click the button, all 500 threads in `job-service` get stuck waiting for the dead IP, exhausting Tomcat's thread pool and causing `job-service` to crash too! This is called a **Cascading Failure**.

#### Scenario WITH Eureka (The Self-Healing Rescue):
```
                     ┌──────────────────────────────┐
                     │    EUREKA SERVER (Port 8761) │
                     │  No heartbeat for 90s!       │
                     │  ❌ EVICTS Dead Port 8082    │
                     │  ✅ KEEPS Healthy Port 8083  │
                     └──────────────▲───────────────┘
                                    │
                                    │ Eureka returns only healthy Port 8083!
                                    │
[ Candidate ] ──▶ [ job-service ] ──┴──────────▶ [ employer-service (Port 8083) ] (ALIVE! ✅)
```
1. Every 30 seconds, instances send a **Heartbeat ping** to Eureka: *"I am alive!"*.
2. When Port 8082 crashes, heartbeats stop.
3. Eureka waits for 90 seconds. With no heartbeat, Eureka **automatically evicts (removes) Port 8082 from the registry**.
4. When `job-service` makes its next call, Eureka provides only the healthy instance (Port 8083).
5. The candidate's request succeeds in **15 milliseconds**! Zero downtime, zero frozen screens.

---

### Doubt 4: `application.properties` vs. `application.yml` — Why YAML here and where is the port set?

#### The Short Answer:
They do the **exact same job**. Spring Boot supports both `.properties` and `.yml` natively. You can use either one!

#### Side-by-Side Comparison:

```properties
# ==========================================
# Option A: application.properties (Flat)
# ==========================================
server.port=8761
spring.application.name=eureka-server
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

```yaml
# ==========================================
# Option B: application.yml (Hierarchical Tree)
# ==========================================
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### Why YAML is popular in Microservices:
1. **No repetitive prefixes:** In `.properties`, you have to type `eureka.client....` 5 times. In YAML, you write `eureka:` once, `client:` once, and indent the properties under it like a tree.
2. **Standard in Cloud & Kubernetes:** Docker Compose, Kubernetes, and GitHub Actions all use YAML, so microservices engineers usually prefer YAML for consistency across configurations.

#### Can we use `application.properties` instead?
**YES, 100%!** If you prefer `application.properties` because it is what you used in `jss-practice`, we can use `application.properties` for `eureka-server` and all our microservices! The port is set with `server.port=8761`.


