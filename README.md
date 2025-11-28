# Task Manager API

## Overview
The **Task Manager API** is a simple RESTful service for managing tasks with full CRUD operations.  
Each task has:

- `id` (auto-generated)
- `title`
- `description`
- `completed` (boolean)
- `createdAt`
- `updatedAt`

The service is built with **Spring Boot**, backed by a **MySQL** database, and packaged as a Docker image.  
An OpenAPI/Swagger UI is exposed so the API can be explored and tested via a browser but can be also tested with postman as well.

## Architecture
### High-level design
- **Controller layer** (`TaskController`)
    - Exposes REST endpoints under `/tasks`
    - Maps HTTP requests to service methods
    - Validates request bodies using `TaskRequest` with Jakarta Bean Validation annotations (`@NotBlank`, `@Size`, etc.).

- **Service layer** (`TaskService`)
    - Contains the business logic for creating, reading, updating, and deleting tasks.
    - Uses `@Transactional` to ensure write operations are executed atomically.
    - Throws a custom `TaskNotFoundException` when a requested task does not exist.

- **Repository layer** (`TaskRepository`)
    - Extends `JpaRepository<Task, Long>` to provide CRUD access to the database.
    - Uses Spring Data JPA to generate queries based on method names.

- **Domain model** (`Task`)
    - JPA entity mapped to the `tasks` table via `@Entity` and `@Table`.
    - Uses `@PrePersist` and `@PreUpdate` callbacks to automatically manage `createdAt` and `updatedAt` timestamps.

- **API contract**
    - OpenAPI definition is provided in `openapi.yaml`.
    - Swagger UI is available at `/swagger-ui.html` when the application is running.

### Patterns & decisions
- **Layered architecture** (Controller → Service → Repository → Database)
- **DTO for input** (`TaskRequest`) to decouple external API contracts from internal entities.
- **Exception handling** via a `@ControllerAdvice` (if present) to return consistent error responses matching the `Error` schema from `openapi.yaml`.
- **Environment-based configuration** using `application.properties` with support for `DB_HOST`, `DB_USER`, and `DB_PASSWORD` environment variables so it works both locally and inside Docker.


## Prerequisites

- **Java 21** (Temurin / OpenJDK)
- **Gradle Wrapper** (included as `./gradlew`)
- **Docker** and **Docker Compose**
- **Git** (if cloning from GitHub)
- Postman or any HTTP client to test the endpoints

## Quick Start
```bash
# Single command to run the entire application
./run.sh
```
if unable to run, make it executable:
```
chmod +x run.sh
./run.sh
```
or 
```
docker compose up --build
```
    
## API Documentation
- **OpenAPI Specification**: Available at `/swagger-ui.html` when running
- **Postman Collection**: Import `postman_collection.json` for testing (optional)

### Testing with Postman

You can test the API using Postman or any similar HTTP client.

### 1. Start the application

If you’re using Docker Compose:

```bash
docker compose up --build
```
The API will be available at:

http://localhost:8080

### 2. Import the Postman collection (optional)

1. Open Postman.

2. Click Import.

3. Select postman_collection.json from this project.

4. Make sure the collection’s base URL is set to: http://localhost:8080.

### 3. Example requests
#### a) Create a task

- Method: `POST`
- URL: http://localhost:8080/tasks
- Body > raw > JSON:
```
{
  "title": "First task",
  "description": "Write IT5001 assignment",
  "completed": false
}
```
Expected response: 201 Created with JSON similar to:

```
{
  "id": 1,
  "title": "First task",
  "description": "Write IT5001 assignment",
  "completed": false,
  "createdAt": "2025-11-16T05:24:47.394927222Z",
  "updatedAt": "2025-11-16T05:24:47.394927222Z"
}

```

#### b) Get all tasks

- Method: `GET`
- URL: http://localhost:8080/tasks
- Expected: `200 OK` with an array of tasks:
```
[
  {
    "id": 1,
    "title": "First task",
    "description": "Write IT5001 assignment",
    "completed": false,
    "createdAt": "...",
    "updatedAt": "..."
  }
]
```
#### c) Get task by ID

- Method: `GET`
- URL: http://localhost:8080/tasks/1
- Expected: `200 OK` with one task.

If the task does not exist (e.g. /tasks/9999):
- `404 Not Found` with an Error JSON body as defined in openapi.yaml.

#### d) Update a task

- Method: `PUT`
- URL: http://localhost:8080/tasks/1
- Body → raw → JSON:
```  
{
  "title": "Updated first task",
  "description": "Write IT5001 assignment (updated)",
  "completed": true
}
```
Expected: `200 OK` with the updated task.

#### e) Delete a task

- Method: `DELETE`
- URL: http://localhost:8080/tasks/1

Expected: `204 No Content`:

After this, a `GET` http://localhost:8080/tasks/1 should return `404 Not Found`.

## Testing
The project uses JUnit 5 and Spring Boot Test


### Unit Tests
- Focus on individual components (e.g., `TaskService`) using mocks (Mockito) for `TaskRepository`.

- Validate that business logic (create, update, delete, error paths) behaves correctly.

```bash
./gradlew test
```

### Integration Tests
integration tests are included in the test phase


- Use `@SpringBootTest` + (optionally) `MockMvc` to load the full Spring context.

- Exercise the REST endpoints end-to-end against an in-memory H2 database configured in the test scope.

- Verify HTTP status codes, response payloads, and error handling (e.g., `404` for missing tasks, `400` for invalid input).

- Currently, both unit and integration tests live under `src/test/java` and are executed together via the standard Gradle `test` task.


## Database Schema
The database has a single main table: `tasks`
```sql
CREATE TABLE tasks (
    id          bigint       not null auto_increment primary key,
    title       varchar(255) not null,
    description text,
    completed   bit          not null,
    created_at  datetime(6)  not null,
    updated_at  datetime(6)  not null
) engine=InnoDB;
```
Schema is managed by JPA/Hibernate via `spring.jpa.hibernate.ddl-auto=update` (for dev/demo).

On startup, Hibernate will:

- Create the `tasks` table if it does not exist.
- Apply basic schema evolution automatically during development.
- For production, a migration tool such as Flyway or Liquibase would be preferable, but is out of scope for this assignment.

## CI/CD Pipeline
The project uses GitHub Actions for Continuous Integration, with a two-job pipeline:

### 1. `test` job

- Triggers on:
  - `push` to `main`
  - `pull_request` targeting `main`

- Steps:

    - Check out the repository.

    - Set up JDK 21 with Gradle dependency caching.

    - Make gradlew executable.

    - Run ./gradlew clean test (executes all unit and integration tests).
- acts as main quality gate: if tests fail, the pipeline stops.


###2. `docker-build` job

- Depends on `test` (`needs: test`) → only runs if tests pass.

- Only runs on `push` to `main` (not on PRs).

  - Steps:

      - Log in to GitHub Container Registry (GHCR) using the `GITHUB_TOKEN`.

      - Configure Docker Buildx.

      - Build and push the app image using `docker/build-push-action` with:

        - Tags:

          - `ghcr.io/<owner>/<repo>/taskmanager-app:latest`

          - `ghcr.io/<owner>/<repo>/taskmanager-app:<git-sha>`

    - Docker layer caching via `cache-from` / `cache-to` (`type=gha`).

### 2. `deploy` job: Local deployment vs CI

- Locally, you do the following:

  - Run tests with ./gradlew test.

  - Run the app + MySQL using docker compose up --build.

- In CI, the same test command is executed on a clean Ubuntu runner, and only if the tests succeed is the Docker image built & pushed.


## Assumptions Made
- The service is running in a trusted environment, so no authentication/authorization is implemented for the API endpoints.

- The API will be consumed by internal tools or trusted clients (hence no rate limiting, API keys, or OAuth).

- hibernate.ddl-auto=update is acceptable for this assignment and development scenarios.

- Using a single database schema (taskmanager) is sufficient; no multi-tenancy or schema versioning is required.

- Error responses can follow a simple structure { message, timestamp, details } as defined in the Error schema in openapi.yaml.

## Known Limitations
Limitations due to time constraints and/or due to the app running local. These will be natural future improvements if the service is promoted beyond an assignment/demo.

- Any client with network access can call the endpoints.

- All tasks are returned in one shot; this is fine for demos but not ideal for large datasets.

- You cannot filter tasks by completion status, date range, etc.

- Schema changes rely on Hibernate’s ddl-auto instead of explicit migration scripts.

- Basic logs only; no structured logging or observability stack (Prometheus/Grafana, etc.).



## Technology Stack
- Spring Boot 3.5+
- Java 21
- MySQL 8+
- Docker & Docker Compose
- Gradle (with wrapper)
- Spring Web (REST controller)
- Spring Data JPA & Hibernate
- H2 (in-memory database for tests)
- GitHub Actions (CI, Docker image build & push)
- OpenAPI / Swagger UI for interactive documentation
- JUnit 5 and Spring Boot Test for testing
- Mockito for mocking in unit tests
- GitHub Container Registry (GHCR) for container images

## Author
capmerah@hotmail.com

## Github
https://github.com/capmerah/taskmanager


