# Repository Guidelines

## Project Structure & Module Organization
Source code resides under `src/main/java/com/example/base` with domain packages such as `auth`, `user`, `common`, and `security`. Tests mirror this layout inside `src/test/java`. Shared configs and templates live in `src/main/resources`; adjust `application.yml` when changing datasources, cache, or JWT properties. Docs reference materials in `docs/db`, `docs/openapi`, and `docs/日志模块.md` for observability plans. Generated build artifacts belong in `target/` and must stay uncommitted.

## Build, Test, and Development Commands
Use the Maven wrapper or local Maven with Java 21. Common commands:
- `mvn clean verify`: run the full build, unit tests, and package the Spring Boot app.
- `mvn spring-boot:run`: launch the API locally with the default profile.
- `mvn -DskipTests package`: produce a jar quickly when tests are already covered.

## Coding Style & Naming Conventions
Follow Java 21 conventions with 4-space indentation and braces on the same line. REST controllers end with `Controller`, services with `Service`, and MyBatis entities with `Entity`. DTOs stay immutable via Lombok annotations such as `@Value`. Shared beans belong in `com.example.base.common.config`. Favor descriptive method names and keep package-private helpers near their usage.

## Testing Guidelines
Write JUnit 5 tests that mirror the production package structure. Use Mockito for stubs and name test classes `{ClassName}Test` with methods like `shouldLoginWithValidCredentials`. Run `mvn clean verify` before submitting changes. Aim for meaningful coverage on domain logic and guard tenant-sensitive flows.

## Commit & Pull Request Guidelines
Author commits in the imperative mood (e.g., `auth: add refresh token rotation`) and keep changes scoped. Pull requests must explain intent, reference related docs or OpenAPI updates, and include manual or automated test evidence. Provide screenshots for UI-affecting work and confirm CI status before requesting review.

## Security & Configuration Tips
Never commit real credentials; rely on placeholders and environment overrides in `application.yml`. Preserve tenant isolation by honoring `TenantFilter` and the `X-Tenant-Id` header for new endpoints. Extend caching through the existing Caffeine and Redis settings under `common.config` when needed, and document any configuration shifts.

## Observability & Logging
Follow the roadmap in `docs/日志模块.md`. Use Logback with `logstash-logback-encoder` for structured JSON logs and keep MDC keys such as `tenantId`, `traceId`, and `userId` populated. Configure Micrometer Tracing with OTLP exporters and align Fluent Bit or Grafana Agent settings so logs flow to Loki and traces to Tempo. Validate end-to-end observability before merging features that emit new logs.
