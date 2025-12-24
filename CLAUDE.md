# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Code Style Rules

- **Single-line if statements**: Use `if (condition) doSomething();` not `if (condition) { doSomething(); }`
- **No comments**: Do not generate comments unless explicitly requested
- **SOLID principles**: Follow Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **KISS**: Keep code simple and effective - avoid over-engineering
- **Logging**: Use `@Slf4j` annotation from Lombok. Use double `::` to separate labels from values: `log.info("User registered with connectId:: {}", id)`
- **Constructors**: Use `@RequiredArgsConstructor` from Lombok for dependency injection instead of `@Autowired`
- **Method spacing**: On larger methods, add blank line after method header and between logical groupings. Smaller methods can be compact.

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
mvn test -Dtest="SampleServiceTest"
```

## Tech Stack

- **Spring Boot 4.0.1** with Java 25
- **Firebase Firestore** (NoSQL database) - emulator auto-starts in dev/test profiles
- **Spring Security** with JWT authentication
- **SpringDoc OpenAPI** for Swagger documentation
- **Cucumber** for BDD testing
- **Lombok** for boilerplate reduction

## Architecture

### Package Structure

```
com.tpg.connect/
├── config/                    # Spring configurations (Security, CORS, Firestore, OpenAPI)
├── security/jwt/              # JWT service for token generation/validation
├── common/constants/          # Shared constants
└── session_authentication/    # Authentication domain
    ├── common/
    │   ├── enums/             # UserRoles enum
    │   └── services/          # ConnectIdGenerationService (Snowflake IDs)
    └── user_registration/
        ├── controller/        # REST controllers
        │   └── api/           # Interface contracts (RegisterApi, LoginApi, LogoutApi)
        ├── model/
        │   ├── entity/        # Firestore document models
        │   ├── request/       # API request DTOs
        │   └── response/      # API response DTOs
        └── repository/        # Firestore data access
            └── api/           # Repository interfaces
```

### Key Patterns

- **Interface-first controllers**: API contracts defined in `controller/api/` interfaces with Swagger annotations, implemented by controllers
- **Snowflake ID generation**: `ConnectIdGenerationService` generates unique, time-sortable 64-bit IDs without database calls
- **Firestore repositories**: Convert Java records to Maps for Firestore storage
- **OpenAPI schema customization**: `OpenApiSchemaConfig` adds descriptions/examples to DTOs without polluting model classes

### API Base Path

All endpoints are prefixed with `/api` (configured via `server.servlet.context-path`).

- Swagger UI: `http://localhost:9000/api/swagger-ui.html`
- OpenAPI spec: `http://localhost:9000/api/api-docs`

### Profiles

- `dev`: Port 9000, Firebase emulator on 8080, debug logging
- `test`: Port 10000, Firebase emulator, Cucumber tests

### Firebase Emulator

The `FirebaseEmulatorConfig` automatically starts/stops the Firebase Firestore emulator in dev/test profiles. Requires Firebase CLI installed (`firebase` command available).