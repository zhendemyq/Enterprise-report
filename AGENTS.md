# Repository Guidelines

## Project Structure & Module Organization
- `backend/`: Spring Boot service. Application code in `src/main/java`, configuration in `src/main/resources`, integration/unit tests in `src/test/java`. Build outputs in `target/`.
- `frontend/`: Vue 3 + Vite app. Source in `src/`, entry at `src/main.js`, static HTML shell in `index.html`, build output in `dist/`. Node modules are local to `frontend/node_modules/`.
- Logs and generated assets stay within each module (`backend/logs/`, `target/`, `frontend/dist/`); do not commit them.

## Build, Test, and Development Commands
- Backend dev: `cd backend && ./mvnw spring-boot:run` (hot reload via Spring DevTools if configured).
- Backend package: `cd backend && ./mvnw clean package` (creates runnable jar in `target/`).
- Backend tests: `cd backend && ./mvnw test` (runs JUnit/MyBatis-Plus/sa-token related tests).
- Frontend dev: `cd frontend && npm install && npm run dev` (Vite dev server with HMR).
- Frontend build: `cd frontend && npm run build` (emits `frontend/dist/`).
- Frontend lint: `cd frontend && npm run lint` (ESLint with Vue plugin; fixes auto-applied where safe).

## Coding Style & Naming Conventions
- Java: Favor Spring idioms, constructor injection, and Lombok annotations already in use. Package by feature (e.g., `com.example.report.{domain, service, controller}`). Use 4-space indentation; avoid wildcard imports; keep DTOs immutable where possible.
- Vue/JS: Use `<script setup>` where reasonable, kebab-case for component filenames (`report-table.vue`), and PascalCase component names in templates. Prefer composables for shared logic. Use `src/api/` for HTTP clients and `src/stores/` for Pinia stores.
- General: Keep config in `application-*.yml` (backend) and `.env.*` (frontend). Name branches descriptively (`feature/report-export`, `fix/auth-timeout`).

## Testing Guidelines
- Backend: Place tests under `backend/src/test/java`; mirror package paths. Name classes `*Test`. Mock external services; use `@SpringBootTest` only when integration is needed. Aim to cover controllers, services, and MyBatis mappers that touch business rules.
- Frontend: Add component tests where applicable (Vitest-compatible setup if added). For now, prefer lightweight integration via E2E smoke scripts or story-style manual checks when wiring new views.

## Commit & Pull Request Guidelines
- Commits: Write imperative summaries (e.g., `Add report export endpoint`, `Fix PDF pagination`). Keep them scoped and small; reference ticket IDs when available.
- Pull Requests: Include purpose, key changes, test evidence (`mvn test`, `npm run build`/`lint` output), and screenshots or GIFs for UI changes. Note any migration steps (new env vars, DB changes, or npm/maven dependencies). Ensure CI passes before request.

## Security & Configuration Notes
- Secrets: Keep credentials out of git; use environment variables or `application-*.yml` with placeholders. Do not commit `.env*` files containing secrets.
- Data sources: Verify MySQL/Redis connection strings before running; prefer using local `.env` or profile-specific YAML. Rotate tokens/keys if checked into logs.

## Agent-Specific Tips
- Work module-by-module to avoid cross-build noise. Run frontend commands from `frontend/` and backend commands from `backend/` to respect relative paths. Preserve existing formatting tools (ESLint, Maven) and avoid adding global installs; rely on project-local toolchains.
