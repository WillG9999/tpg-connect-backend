# TPG Connect API

A Spring Boot REST API application for TPG Connect services.

## Quick Start

### Prerequisites
- Java 25
- Maven 3.6+

### Running the Application

```bash
# Run with default profile
mvn spring-boot:run

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Building

```bash
# Build the project
mvn clean compile

# Package the application
mvn clean package

# Run tests
mvn test
```

## API Documentation

Once the application is running, you can access the API documentation:

- **Swagger UI**: http://localhost:9000/api/swagger-ui.html
- **OpenAPI Spec**: http://localhost:9000/v3/api-docs

## Development Servers

- **Development**: http://localhost:9000
- **Test**: http://localhost:10000

## Testing

```bash
# Run all tests
mvn test

# Run unit tests only
mvn test -Dtest="**/*Test"

# Run integration tests
mvn test -Dtest="CucumberTestRunner"
```

## Technology Stack

- **Spring Boot 4.0.1**
- **Java 25**
- **Maven**
- **Spring Web MVC & WebFlux**
- **SpringDoc OpenAPI**
- **Cucumber** for BDD testing
- **Project Lombok**

## Contact

**TPG Team**
- Email: support@tpg.com

## Project Structure

```
src/
├── main/
│   ├── java/com/tpg/connect/
│   │   ├── ConnectApplication.java
│   │   └── config/
│   │       └── OpenApiConfig.java
│   └── resources/
│       ├── application.properties
│       └── application-dev.properties
└── test/
    ├── java/com/tpg/connect/
    │   ├── integration/
    │   └── unit/
    └── resources/
        └── features/
```