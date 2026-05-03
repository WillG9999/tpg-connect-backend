# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

This is a **Spring Boot 4.0.1 / Java 25 / Maven** backend API for the TPG Connect app. It handles authentication,
user profiles, matching, conversations, and safety features. Data is persisted in **Firebase Firestore**. Redis is
used for rate limiting and token blacklisting.

---

## Build & Run Commands

```bash
# Run with dev profile (starts Firebase emulator automatically)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Build
mvn clean compile

# Run all tests
mvn test

# Run unit tests only
mvn test -Dtest="**/*Test"

# Run Cucumber integration tests
mvn test -Dtest="CucumberTestRunner"

# Run a single test class
mvn test -Dtest="RegistrationControllerTest"
```

---

## Architecture

```
Controller (implements Api interface)
    └── Service
            ├── Mapper (@Component)
            ├── Repository (Firestore)
            └── Factory / Component
```

- **Api interface** — Defines the REST contract with OpenAPI annotations and route mappings. Lives in `controller/api/`.
- **Controller** — Thin layer; implements the Api interface. Delegates to service. No business logic.
- **Service** — Business logic, orchestration, error handling.
- **Mapper** — `@Component` class that converts between Firestore `DocumentSnapshot`/`Map` and entity/response types.
- **Repository** — Firestore data access. Exposes an interface (`{Domain}RepositoryApi`) implemented by the concrete class.
- **Factory** — Creates domain entities from request objects (when construction is non-trivial).
- **Component** — Utility classes for ID generation, code generation, etc.

### Package Structure

```
com.tpg.connect/
├── config/                    # Spring configs (Security, CORS, Firestore, Redis, OpenAPI, OpenTelemetry)
├── common/
│   ├── constants/             # Shared constants (endpoints, headers, repository names)
│   ├── exceptions/            # GlobalExceptionHandler, shared exceptions
│   ├── jsonwebtoken/          # JWT provider, validator, auth filter
│   ├── ratelimit/             # Bucket4j rate limiting (filter, service, plans)
│   ├── security/              # Token blacklist, refresh tokens, UserRole enum
│   ├── services/              # Shared services (PasswordService, JWT auth services)
│   ├── storage/               # Photo storage service (GCS / emulator)
│   └── tracing/               # TracingFilter (OpenTelemetry)
├── {domain}/                  # One package per feature domain (see domains below)
│   ├── controller/
│   │   └── api/               # {Feature}Api interface + {Feature}Controller
│   ├── service/               # {Feature}Service
│   ├── mapper/                # {Feature}Mapper (@Component)
│   ├── repository/            # {Feature}Repository + {Feature}RepositoryApi
│   ├── model/
│   │   ├── entity/            # Firestore document models (Java records + @Builder)
│   │   ├── request/           # API request records
│   │   └── response/          # API response records
│   ├── factory/               # Entity factories (if needed)
│   ├── components/            # Utility components (if needed)
│   └── exceptions/            # Domain-specific exceptions (if needed)
```

**Domains:** `user_registration`, `login_logout`, `email_verification`, `password_reset`, `profile`, `application`,
`admin`, `matching`, `conversation`, `settings`, `safety`

### API Base Path

All endpoints are prefixed with `/api` (configured via `server.servlet.context-path`).

- Swagger UI: `http://localhost:9000/api/swagger-ui.html`
- OpenAPI spec: `http://localhost:9000/api/api-docs`

### Profiles

- `dev`: Port 9000, Firebase emulator on 8080, debug logging
- `test`: Port 10000, Firebase emulator, Cucumber tests

---

## Code Style & Conventions

### General

- Java 25 features: records, sealed classes, pattern matching, text blocks where appropriate.
- Follow SOLID principles. Prefer composition over inheritance.
- Constructor injection via Lombok `@RequiredArgsConstructor` — never use `@Autowired`.
- Use `@Slf4j` for logging in every service and controller class.
- **Indentation:** spaces only — never tab characters. 4 spaces per level.
- **Single-line if statements:** `if (condition) doSomething();` — no braces for single-statement bodies.
- **No comments** unless explicitly requested.
- **KISS:** avoid over-engineering. Don't add abstractions the code doesn't need yet.

### Naming

| Artefact          | Convention                                              | Example                         |
|-------------------|---------------------------------------------------------|---------------------------------|
| Controller        | `{Feature}Controller`                                   | `RegistrationController`        |
| Api interface     | `{Feature}Api`                                          | `RegisterApi`                   |
| Service           | `{Feature}Service`                                      | `ApplicationService`            |
| Service interface | `{Feature}ServiceApi`                                   | `LoginServiceApi`               |
| Mapper            | `{Feature}Mapper`                                       | `ApplicationMapper`             |
| Repository        | `{Feature}Repository`                                   | `ApplicationRepository`         |
| Repo interface    | `{Feature}RepositoryApi`                                | `ApplicationRepositoryApi`      |
| Factory           | `{Feature}Factory`                                      | `ApplicationFactory`            |
| Component         | descriptive name                                        | `AppIdGenerator`                |
| Entity            | plain class name (record + `@Builder`)                  | `Application`, `RegisteredUser` |
| Request DTO       | `{Action}Request`                                       | `ApplicationSubmissionRequest`  |
| Response DTO      | `{Action}Response`                                      | `ApplicationSubmissionResponse` |
| Exception         | descriptive name extending `RuntimeException`           | `TokenExpiredException`         |
| Constants         | `{Domain}Constants` — utility class, private constructor | `ConnectApiEndpointConstants`   |
| Config            | `{Feature}Config`                                       | `RedisConfig`                   |

### Models

- **Entities and DTOs:** Java records with `@Builder` (via Lombok).
- **Firestore entities:** records with `@Builder(toBuilder = true)` so partial updates are easy.
- Never use Lombok `@Data` / `@Getter` / `@Setter` — records provide accessors.

### API Interface

- Define route mappings (`@PostMapping`, `@GetMapping`, etc.) and OpenAPI annotations (`@Operation`, `@ApiResponse`,
  `@Parameter`) on the Api interface, not the controller.
- Reference endpoint paths from `ConnectApiEndpointConstants`.
- All endpoint paths use a `/v1/` version prefix (e.g. `/v1/auth/register`).

### Controllers

- Annotate with `@RestController` and `@RequiredArgsConstructor @Slf4j`.
- Implement the corresponding Api interface.
- Delegate entirely to service — no business logic.

### Services

- Annotate with `@Service @RequiredArgsConstructor @Slf4j`.
- Keep methods focused. Extract complex logic into private helpers or dedicated components.

### Mappers

- Annotate with `@Component @RequiredArgsConstructor`.
- Map between `DocumentSnapshot` / `Map<String, Object>` and domain records.
- Keep mapping logic inside the mapper — not in services or repositories.

### Repositories

- Define a `{Feature}RepositoryApi` interface for the public contract.
- Concrete class annotated with `@Service @RequiredArgsConstructor @Slf4j`.
- All Firestore I/O goes through the repository — no Firestore calls in services.
- Convert entities to `Map<String, Object>` via the mapper before writing to Firestore.

### Logging

- Use `@Slf4j`. Separate labels from values with `::`:
  `log.info("Application submitted - applicationId:: {}", applicationId)`
- Log at entry and exit of public service methods (info level).
- Log errors with full context (IDs, inputs, exception).

### Error Handling

- Domain-specific exceptions extend `RuntimeException`.
- Map to HTTP responses via `GlobalExceptionHandler` (`@RestControllerAdvice`).
- Log errors before throwing or in the handler — include IDs and request context.

### Configuration

- Spring configs in `config/` — annotated `@Configuration` or `@ConfigurationProperties`.
- Environment-specific config in `application-{profile}.yml`.
- Sensitive values via environment variables.

---

## Testing Standards

### Unit Tests (JUnit 5 + Mockito)

- Location: `src/test/java/com/tpg/connect/unit/` — mirror main source structure.
- Class name: `{ClassUnderTest}Test`.
- Use `@ExtendWith(MockitoExtension.class)` and `@Mock` on dependencies.
- **Construct the SUT manually in `@BeforeEach`** via its constructor — do not use `@InjectMocks`.
- **Method naming:** `methodName_condition_expectedBehavior`
  (e.g., `submitApplication_success_returnsApplicationId`).
- Use `when(...).thenReturn(...)` + `verify(...)`.
- Static imports for assertions: `assertEquals`, `assertNotNull`, `assertThrows`.
- **Prefer asserting full objects:**
  - For records: `assertEquals(expected, actual)`.
  - Only assert individual fields when testing a specific field transformation.
- Every public method in a service, mapper, or controller must have test coverage.

### Integration Tests (Cucumber BDD)

- Location: `src/test/java/com/tpg/connect/integration/`.
- Runner: `CucumberTestRunner.java` — `@Suite` + `@IncludeEngines("cucumber")`.
- Step definitions in `src/test/java/com/tpg/connect/integration/steps/` — named `{Domain}ApiSteps`.
- Feature files in `src/test/resources/features/` (flat — one feature file per domain).
- Tests run against a live Spring Boot instance (`DEFINED_PORT`, `test` profile, port 10000).
- Use plain `RestTemplate` with `BASE_URL = "http://localhost:10000/api"` to make HTTP calls.
- Firebase and Redis use emulators in the `test` profile — no real infrastructure needed.
- Catch `HttpStatusCodeException` to assert error status codes without test failures.

### When generating tests

1. Prefer unit tests — write one if the class can be tested in isolation.
2. Write a Cucumber scenario for end-to-end API behaviour.
3. Always mock or emulate external dependencies — never call real services.
4. Cover happy path, error paths, and edge cases.

---

## Feature Development Checklist

When adding a new domain or endpoint, generate all of the following under `com.tpg.connect.{domain}/`:

- [ ] `controller/api/{Feature}Api.java` — Interface with OpenAPI annotations and route mappings
- [ ] `controller/api/{Feature}Controller.java` — Implements Api, delegates to service
- [ ] `service/{Feature}Service.java` — Business logic
- [ ] `model/entity/` — Firestore entity records
- [ ] `model/request/` — Request records
- [ ] `model/response/` — Response records
- [ ] `mapper/{Feature}Mapper.java` — Firestore ↔ entity/response mapping
- [ ] `repository/{Feature}RepositoryApi.java` — Repository interface
- [ ] `repository/{Feature}Repository.java` — Firestore implementation
- [ ] `exceptions/` — Domain exceptions (if needed)
- [ ] Unit tests in `src/test/java/com/tpg/connect/unit/{domain}/`
- [ ] Cucumber scenario and step definitions (for API-level tests)
- [ ] Endpoint constant in `ConnectApiEndpointConstants`
- [ ] Security config update in `SecurityConfig` if endpoint has different auth rules

---

## Key Dependencies

| Dependency                  | Usage                                              |
|-----------------------------|----------------------------------------------------|
| Spring Boot 4.0.1           | Application framework                              |
| Java 25                     | Language runtime                                   |
| Firebase Admin SDK 9.4.1    | Firestore NoSQL database                           |
| Spring Security             | Auth/authorization                                 |
| jjwt 0.12.6                 | JWT generation and validation                      |
| Spring Data Redis + Lettuce | Token blacklist, refresh tokens                    |
| Bucket4j 8.10.1             | Rate limiting (Redis-backed)                       |
| Embedded Redis 1.4.3        | Redis emulator for tests                           |
| Springdoc OpenAPI 2.6.0     | Swagger / OpenAPI documentation                    |
| OpenTelemetry 1.40.0        | Distributed tracing                                |
| Lombok                      | `@RequiredArgsConstructor`, `@Slf4j`, `@Builder`   |
| Spring Boot Mail            | Email (verification codes, password reset)         |
| Thymeleaf                   | Email templates                                    |
| GCS (google-cloud-storage)  | Photo storage                                      |
| Spring WebSocket            | Real-time chat                                     |
| JUnit 5 + Mockito           | Unit testing                                       |
| Cucumber 7.31.0             | BDD integration testing                            |
| WireMock 2.35.1             | External HTTP stubbing in tests                    |
