# Repository Guidelines

## Project Structure & Module Organization
- Source lives under `src/main/java/com/example/base/...` with domain packages such as `auth`, `user`, `common`, and `security`.
- Tests mirror production code under `src/test/java`, following identical package names.
- Shared configs and templates sit in `src/main/resources`; adjust `application.yml` when datasource, cache, or JWT settings change.
- Reference docs in `docs/db`, `docs/openapi` to keep observability and schema updates aligned.
- Maven build output belongs in `target/` and must stay uncommitted.

## Build, Test, and Development Commands
- `./mvnw clean verify`: run the full build, execute tests, and package the Spring Boot app.
- `./mvnw spring-boot:run`: launch the API locally using the default profile.
- `./mvnw -DskipTests package`: produce a jar quickly once tests already pass.
- Use Java 21 locally; keep environment overrides in profiles or `.env`, never in VCS.

## Coding Style & Naming Conventions
- Java 21 with 4-space indentation and braces on the same line.
- REST controllers end with `Controller`, services with `Service`, MyBatis entities with `Entity`, and DTOs stay immutable via Lombok `@Value`.
- Place shared beans in `com.example.base.common.config`; keep package-private helpers close to consumers.
- Favour descriptive methods and add brief comments only for non-obvious logic.

## Testing Guidelines
- Rely on JUnit 5 and Mockito; mirror production packages (e.g., `src/test/java/com/example/base/auth`).
- Name test classes `{ClassName}Test` and methods like `shouldLoginWithValidCredentials`.
- Target meaningful coverage on domain and tenant-sensitive flows; run `./mvnw clean verify` before pushing.

## Commit & Pull Request Guidelines
- Write commits in imperative mood (e.g., `auth: add refresh token rotation`) and keep scope tight.
- Pull requests must outline intent, link relevant docs or OpenAPI changes, and attach test evidence or UI screenshots when applicable.
- Confirm CI status and tenant isolation handling prior to requesting review.

## Security & Configuration Tips
- Never commit real credentials; rely on placeholders and profile-based overrides in `application.yml`.
- Maintain tenant isolation via `TenantFilter` and the `X-Tenant-Id` header on new endpoints.
- Extend caching through existing Caffeine and Redis settings under `common.config`; document any configuration shifts.
