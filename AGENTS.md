# Repository Guidelines

## Project Structure & Module Organization
- Application code lives under `src/main/java/com/example/base`, split into domain packages (`auth`, `user`, `common`, `security`).
- Shared configuration and resources are in `src/main/resources`; edit `application.yml` for datasource, cache, and JWT settings.
- Database assets and API contracts are documented in `docs/db` and `docs/openapi`. Reference these before updating entities or controllers.
- Generated artifacts compile into `target/`; avoid committing anything from that directory.

## Build, Test, and Development Commands
- `mvn clean verify` — compile the project, run unit tests, and package the Spring Boot application.
- `mvn spring-boot:run` — launch the API locally with the default profile.
- `mvn -DskipTests package` — produce a jar quickly when tests are unnecessary (CI should still run tests).

## Coding Style & Naming Conventions
- Follow standard Spring Boot/Java 21+ style: 4-space indentation, braces on the same line, Lombok for DTOs where already used.
- Name REST controllers `{Domain}Controller`, services `{Domain}Service`, and map MyBatis entities to `...Entity`. Keep DTOs immutable with Lombok `@Value`.
- Use Caffeine cache and Redis settings already present; new beans belong in `com.example.base.common.config`.

## Testing Guidelines
- Place tests in `src/test/java`, mirroring the main package structure. Use JUnit 5 and Mockito (add dependencies if required).
- Name test classes `{ClassName}Test`, and ensure methods describe behavior, e.g., `shouldLoginWithValidCredentials`.
- Aim for tests around authentication flows (login, refresh, logout) and service-layer logic; integration tests should use an embedded database or Testcontainers.

## Commit & Pull Request Guidelines
- Write commits in imperative mood, prefixed by scope when helpful, e.g., `auth: add refresh token rotation`. Keep commits logically isolated.
- Pull requests must summarize intent, reference OpenAPI specs or docs touched, and list manual/automated test evidence. Include screenshots for UI-affecting changes (if any).
- Ensure CI passes and that new endpoints reflect both service implementations and documentation updates (`docs/openapi/openapi.yaml`).

## Security & Configuration Tips
- Never commit real secrets; use placeholders in `application.yml` and rely on environment overrides.
- Tenant isolation depends on `TenantFilter` and header `X-Tenant-Id`; verify new endpoints read/write tenant-aware data.
