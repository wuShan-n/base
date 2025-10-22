# Repository Guidelines

## Project Structure & Module Organization
This Spring Boot 3.5.x workspace targets Java 25. The root `pom.xml` is the aggregator; add each ready module under its `<modules>` block so Maven can build them together. Domain code sits inside `modules/<module-name>` (`auth-domain`, `common-core`, `common-security`, `common-web`, `infra-mp`, `user-domain`, `web-api`). Follow the standard Maven tree: `src/main/java` for application code, `src/main/resources` for configuration, and `src/test/java` for test sources. Documentation lives in `docs/` (`docs/openapi` for contracts, `docs/db` for schema plans). Start new packages under `com.example` and nest modules (for example `com.example.user`) to keep classpath scanning straightforward.

## Build, Test, and Development Commands
- `mvn clean verify` at the repository root compiles every registered module and runs the full test suite.
- `mvn -pl modules/web-api -am spring-boot:run` bootstraps the Web API module and any required dependencies during local development.
- `mvn -pl modules/<module> test` limits the lifecycle to a single module while iterating on focused changes.
Point your IDE at the JDK 25 toolchain and retain the Maven cache (`~/.m2`) to speed up local and CI builds. Add the Maven wrapper (`mvnw`) if contributors need a pinned Maven version.

## Coding Style & Naming Conventions
Use four-space indentation and LF line endings. Order imports `java.*`, `jakarta.*`, third-party, then project packages. Apply UpperCamelCase to classes and enums; use lowerCamelCase for methods, fields, and variables; prefer kebab-case for configuration files (`application-local.yml`). Favor constructor injection for Spring beans, keep Lombok annotations focused on removing boilerplate, and locate shared helpers in `common-core` or `common-web` rather than duplicating code across modules.

## Testing Guidelines
Testing relies on `spring-boot-starter-test` with JUnit 5, Mockito, and AssertJ. Place unit tests in `src/test/java` with a `*Test` suffix, exercising narrow slices. Use `@SpringBootTest` or Testcontainers only for integration flows and suffix those classes `*IT`. Run `mvn -pl modules/<module> test` before pushing and target >=80% coverage on new code. Keep fixtures in `src/test/resources` and document significant scenarios in `docs/openapi` or `docs/db` when they influence contracts or schema.

## Commit & Pull Request Guidelines
Follow Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`) and add a module scope when it adds clarity (`feat(web-api): add session endpoints`). Keep commits focused, rebased, and build-clean. Pull requests must describe the change, list migrations or configuration updates, and paste the latest `mvn clean verify` result. Link relevant tickets and include screenshots or OpenAPI diffs whenever functional behavior or contracts change.
