# ArchitectAI Backend

Spring Boot backend for the ArchitectAI MVP.

The current backend implements requirement intake, local AI extraction, validation, persistence, and API responses. It now generates deterministic layout/floorplan data and a render prompt, but it still does not run async render jobs or create render images.

## Backend Architecture

```text
POST /api/designs
  |
  v
DesignController
  |
  v
DesignService
  |
  +-- create DesignProject(PENDING)
  +-- RequirementExtractor
  |     |
  |     +-- load prompt from resources
  |     +-- OllamaClient.complete(...)
  |     +-- parse DesignBrief JSON
  |     +-- validate dimensions
  |
  +-- save DesignOutput.designBriefJson
  +-- save AiCall log
  +-- mark project COMPLETED or FAILED
```

The backend is a single Spring Boot application. There are no workers, queues, schedulers, or separate services.

## Package Structure

```text
src/main/java/com/architectai/
+-- ArchitectAiBackendApplication.java     # Spring Boot entry point
+-- ai/                                    # Ollama extraction and AI call logs
|   +-- AiCall.java                        # JPA entity for AI request/response history
|   +-- AiCallRepository.java              # Repository for ai_calls
|   +-- OllamaClient.java                  # Calls local /api/generate
|   +-- RequirementExtractionResult.java   # DesignBrief plus raw AI response
|   +-- RequirementExtractor.java          # Prompt loading, AI call, JSON parsing
+-- config/
|   +-- JacksonConfig.java                 # Shared ObjectMapper bean
+-- design/                                # Active design workflow
    +-- DesignBrief.java                   # Extracted requirement schema
    +-- DesignController.java              # /api/designs endpoints
    +-- DesignOutput.java                  # JPA entity for generated outputs
    +-- DesignOutputRepository.java        # Lookup output by project id
    +-- DesignProject.java                 # JPA entity for submitted projects
    +-- DesignRepository.java              # Repository for design_projects
    +-- DesignResponse.java                # API response DTO
    +-- DesignService.java                 # Create/load design workflow
```

Resources:

```text
src/main/resources/
+-- application.properties                 # MySQL, JPA, server, Ollama config
+-- prompts/
    +-- design-brief-extraction.md         # Strict JSON extraction prompt
```

Tests:

```text
src/test/java/com/architectai/
+-- BackendApplicationTests.java           # Spring context smoke test
```

## Controllers

### `DesignController`

Package:

```text
com.architectai.design
```

Endpoints:

- `POST /api/designs`
- `GET /api/designs/{id}`

Behavior:

- Accepts request body as `Map<String, String>`.
- Reads `requirement`.
- Returns `400` when `requirement` is missing or blank.
- Delegates creation/loading to `DesignService`.
- Uses `@CrossOrigin` for local frontend development.

## Services

### `DesignService`

Main backend workflow.

Implemented responsibilities:

- Create a `DesignProject`.
- Set initial status to `PENDING`.
- Call `RequirementExtractor`.
- Save extracted `DesignBrief` JSON into `DesignOutput`.
- Save every AI call into `AiCall`.
- Mark project as `COMPLETED` or `FAILED`.
- Load a saved design by project id.

Current statuses:

```text
PENDING
COMPLETED
FAILED
```

## AI Integration

The backend uses local Ollama, not OpenAI.

### `OllamaClient`

Config values:

```properties
ollama.model=qwen2.5-coder:7b
ollama.base-url=http://localhost:11434/api/generate
```

`qwen2.5-coder:7b` is the preferred local model for this backend because requirement extraction depends on deterministic JSON/schema output and Vietnamese prompts; `deepseek-coder:6.7b` remains a reasonable coding-focused alternative but is not the default.

Request behavior:

- Calls Ollama `POST /api/generate`.
- Uses `stream=false`.
- Uses `temperature=0`.
- Uses `num_predict=1000`.
- Sends one combined prompt: system prompt plus user requirement.

### `RequirementExtractor`

Implemented flow:

1. Load `prompts/design-brief-extraction.md` from classpath.
2. Call Ollama.
3. Strip Markdown code fences if the model returns fenced JSON.
4. Parse JSON into `DesignBrief`.
5. Validate `siteWidthMeters` and `siteDepthMeters`.

Validation:

```text
siteWidthMeters > 0
siteDepthMeters > 0
```

Invalid JSON raises:

```text
Unable to parse DesignBrief JSON from AI response
```

Invalid dimensions raise:

```text
Site width and depth must be greater than 0 meters
```

### Prompt

Prompt file:

```text
src/main/resources/prompts/design-brief-extraction.md
```

Expected output schema:

```json
{
  "siteWidthMeters": 5,
  "siteDepthMeters": 20,
  "floors": 2,
  "bedrooms": 3,
  "bathrooms": 2,
  "style": "modern",
  "rooms": ["living", "kitchen", "bedroom", "bathroom"],
  "preferences": ["small front yard"],
  "constraints": []
}
```

AI rules:

- Return JSON only.
- Understand Vietnamese and unaccented Vietnamese.
- Parse dimensions like `5x20m`.
- Default missing floors, bedrooms, bathrooms, style, rooms, preferences, and constraints.
- Use `0` for missing site dimensions so Java validation can reject the request.
- Do not generate final room sizes, geometry, coordinates, walls, doors, or windows.

## DTOs

### `DesignBrief`

Java record:

```java
public record DesignBrief(
        double siteWidthMeters,
        double siteDepthMeters,
        int floors,
        int bedrooms,
        int bathrooms,
        String style,
        List<String> rooms,
        List<String> preferences,
        List<String> constraints
) {}
```

It uses `@JsonIgnoreProperties(ignoreUnknown = true)`.

### `DesignResponse`

Java record:

```java
public record DesignResponse(
        Long projectId,
        String status,
        DesignBrief designBrief,
        String error
) {}
```

## Persistence Layer

Repositories:

```text
DesignRepository              # JpaRepository<DesignProject, Long>
DesignOutputRepository        # JpaRepository<DesignOutput, Long>
AiCallRepository              # JpaRepository<AiCall, Long>
```

`DesignOutputRepository` adds:

```java
Optional<DesignOutput> findByProjectId(Long projectId);
```

Hibernate creates/updates tables because:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Database Schema

### `design_projects`

```text
id                  bigint primary key
title               varchar
raw_requirement     text
status              varchar
created_at          datetime
updated_at          datetime
```

Used now:

- Stores the submitted requirement.
- Tracks project status.

### `design_outputs`

```text
id                  bigint primary key
project_id          bigint foreign key to design_projects.id
design_brief_json   text
rule_result_json    text
layout_plan_json    text
floorplan_json      text
svg_path            varchar
render_prompt       text
render_image_path   varchar
created_at          datetime
updated_at          datetime
```

Used now:

- `design_brief_json`
- `rule_result_json`
- `layout_plan_json`
- `floorplan_json`
- `svg_path`
- `render_prompt`

Scaffolded for later MVP steps:

- `render_image_path`

### `ai_calls`

```text
id                  bigint primary key
project_id          bigint foreign key to design_projects.id
stage               varchar
model               varchar
prompt_text         text
response_text       text
success             boolean
error_message       text
created_at          datetime
```

Used now:

- Logs `DESIGN_BRIEF` extraction attempts.
- Stores raw user requirement as `prompt_text`.
- Stores raw Ollama response as `response_text`.

## API Endpoints

### Create design

```http
POST /api/designs
Content-Type: application/json
```

Request:

```json
{
  "requirement": "Nha pho 5x20m, 2 tang, 3 phong ngu, 2 WC, phong cach hien dai."
}
```

Success response:

```json
{
  "projectId": 1,
  "status": "COMPLETED",
  "designBrief": {
    "siteWidthMeters": 5,
    "siteDepthMeters": 20,
    "floors": 2,
    "bedrooms": 3,
    "bathrooms": 2,
    "style": "modern",
    "rooms": ["living", "kitchen", "bedroom", "bathroom"],
    "preferences": [],
    "constraints": []
  },
  "error": null
}
```

Failed extraction response:

```json
{
  "projectId": 1,
  "status": "FAILED",
  "designBrief": null,
  "error": "Site width and depth must be greater than 0 meters"
}
```

Bad request:

```json
{
  "error": "requirement is required"
}
```

### Get design

```http
GET /api/designs/{id}
```

Returns:

- `200` with `DesignResponse` when found.
- `404` when project id does not exist.

## Rule Engine

No rule engine exists yet.

Current validation is only:

```text
siteWidthMeters > 0
siteDepthMeters > 0
```

There is no:

- `rules` package.
- `VietnameseRuleEngine`.
- `RuleResult` DTO.
- Rule API field in `DesignResponse`.

When added, keep it deterministic Java code. AI should not be the rule engine.

## Layout Generation

No layout generation exists yet.

There is no:

- layout package.
- layout planner.
- layout DTO.
- room placement logic.

`design_outputs.layout_plan_json` is a scaffolded column only.

## Geometry Generation

No geometry generation exists yet.

There is no:

- geometry package.
- floorplan model.
- room rectangle generation.
- wall, door, or window generation.

`design_outputs.floorplan_json` is a scaffolded column only.

## SVG Generation

No SVG generation exists yet.

There is no:

- SVG renderer.
- artifact controller.
- local file storage service.
- `/api/artifacts/{filename}` endpoint.

`design_outputs.svg_path` is a scaffolded column only.

## Scheduler Jobs

No scheduler jobs exist.

There is no:

- `@Scheduled` usage.
- render job entity.
- background worker.
- polling endpoint.

Current generation is synchronous inside `POST /api/designs`.

## Local Backend Development

Requirements:

- Java 21+
- MySQL 8+
- Ollama
- Ollama model `qwen2.5-coder:7b`
  - Preferred over `deepseek-coder:6.7b` for JSON/schema extraction from Vietnamese requirements.

Create database:

```sql
CREATE DATABASE architect_ai;
```

Current config:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/architect_ai
spring.datasource.username=root
spring.datasource.password=sa123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
server.port=8080
ollama.model=qwen2.5-coder:7b
ollama.base-url=http://localhost:11434/api/generate
```

Run backend on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Run tests:

```powershell
.\mvnw.cmd test
```

Backend URL:

```text
http://localhost:8080
```

## Backend Shipping Rule

Keep backend work direct:

```text
Controller -> Service -> Deterministic Java logic -> JPA
```

Do not add queues, workers, microservices, or workflow engines for this MVP.
