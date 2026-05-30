# ArchitectAI

MVP-first AI-powered Vietnamese housing floorplan generator.

The current implementation accepts a Vietnamese housing requirement, extracts a structured `DesignBrief` with local Ollama, evaluates deterministic rules, generates a layout/floorplan, stores the result in MySQL, and displays SVG/JSON/3D previews plus a render prompt in the React frontend.

## MVP Philosophy

Keep the system small enough for one developer to ship quickly.

```text
AI extracts intent.
Java validates and stores deterministic data.
SVG will visualize the floorplan.
React displays the result.
```

Do not add platform architecture before the vertical slice works.

## Architecture

```text
User
  |
  v
React + Vite frontend
  |
  | POST /api/designs
  v
Spring Boot backend
  |
  +-- Load extraction prompt
  +-- Call local Ollama
  +-- Parse DesignBrief JSON
  +-- Validate site dimensions
  +-- Save project, output, and AI log
  |
  v
MySQL
```

There is no queue, worker, scheduler, Docker setup, migration system, or CI pipeline in the current app.

## Data Flow

1. User submits a house requirement in the React form.
2. Frontend calls `POST http://localhost:8080/api/designs`.
3. Backend creates a `design_projects` row with status `PENDING`.
4. Backend calls Ollama using `backend/src/main/resources/prompts/design-brief-extraction.md`.
5. Backend parses the AI response into `DesignBrief`.
6. Backend validates width and depth are greater than `0`.
7. Backend saves `design_outputs.design_brief_json`.
8. Backend saves an `ai_calls` log row.
9. Project becomes `COMPLETED` or `FAILED`.
10. Frontend displays the response.

## Tech Stack

Backend:

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- MySQL
- Jackson
- Ollama local API

Frontend:

- React 19
- Vite 8
- Tailwind CSS 4
- ESLint 10
- Fetch API for the implemented backend call

Installed but not used by the current frontend screen:

- Three.js / React Three Fiber / Drei
- Konva / React Konva
- Axios
- Lucide React
- React Router

## Folder Overview

```text
.
+-- backend/                         # Spring Boot API, AI extraction, JPA persistence
|   +-- READMEbackend.md             # Backend implementation details
|   +-- src/main/java/com/architectai/
|   |   +-- ai/                      # Ollama extraction and AI call logging
|   |   +-- config/                  # ObjectMapper configuration
|   |   +-- design/                  # Design API, service, DTOs, entities
|   +-- src/main/resources/
|       +-- application.properties   # MySQL, port, Ollama settings
|       +-- prompts/                 # AI extraction prompt
+-- frontend/                        # React + Vite app
|   +-- READMEfrontend.md            # Frontend implementation details
|   +-- src/
|       +-- features/design/         # Active design form/result workflow
|       +-- components/              # Unused chat-style components
+-- prompts/                         # Extra prompt note file
+-- docs/                            # Empty
+-- assests/                         # Empty, misspelled folder name
+-- .github/java-upgrade/            # Tool hook scripts, not app CI
+-- README.md
```

## Frontend and Backend Contract

The active frontend screen calls one backend endpoint:

```http
POST /api/designs
```

Request:

```json
{
  "requirement": "Nha pho 5x20m, 2 tang, 3 phong ngu, 2 WC, phong cach hien dai."
}
```

Response:

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
  "renderPrompt": "Architectural visualization prompt: Vietnamese townhouse on a 5m x 20m urban lot...",
  "renderImagePath": null,
  "error": null
}
```

## Quick Local Setup

Requirements:

- Java 21+
- Node.js 18+
- MySQL 8+
- Ollama
- Ollama model `qwen2.5-coder:7b`
  - Chosen over `deepseek-coder:6.7b` because this app needs reliable JSON/schema extraction and Vietnamese natural-language understanding in addition to code-like structured output.

Create the database:

```sql
CREATE DATABASE architect_ai;
```

Pull the model:

```bash
ollama pull qwen2.5-coder:7b
```

Run backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Run frontend:

```bash
cd frontend
npm install
npm run dev
```

Default URLs:

```text
Backend:  http://localhost:8080
Frontend: http://localhost:5173
Ollama:   http://localhost:11434/api/generate
```

## Core Constraints

Keep the MVP simple:

- One Spring Boot backend.
- One React frontend.
- One MySQL database.
- Local Ollama for AI extraction.
- Java owns validation and future deterministic generation.
- React displays backend output.

Do not add:

- Microservices
- Kafka
- Redis
- RabbitMQ
- Kubernetes
- Service mesh
- Event-driven systems
- Vector databases
- RAG architecture
- Multi-tenant systems
- Workflow engines
- Enterprise observability

## 7-Day Build Plan

### Day 1: Project and API skeleton
- Setup backend, frontend, MySQL, Ollama/OpenAI local dev
- Create minimal backend packages
- Create database tables
- Implement `POST /api/designs`
- Implement `GET /api/designs/{id}`
- Create frontend design page and input form
- Ensure end-to-end request works (even if output is mock)

---

### Day 2: DesignBrief extraction
- Add OpenAI/Ollama client
- Write `design-brief-extraction.md` prompt
- Extract strict JSON `DesignBrief`
- Add validation + fallback handling
- Store `DesignBrief` in DB
- Display extracted result in frontend

---

### Day 3: Rule engine
- Implement `VietnameseRuleEngine`
- Add deterministic checks (area, floors, stairs, etc.)
- Generate warnings (not blocking)
- Store `rule_result_json` in `design_outputs`
- Show rule warnings in UI

---

### Day 4: Layout planner
- Implement simple Java layout planner
- Generate `LayoutPlan` deterministically
- (Optional) use AI only for hints, not final layout
- Store `layout_plan_json`
- Display layout JSON in frontend

---

### Day 5: Geometry + SVG
- Generate deterministic floorplan geometry:
  - rooms
  - walls
  - doors
  - windows
- Store `floorplan_json`
- Render SVG in backend
- Save SVG file locally
- Display SVG in frontend

---

### Day 6: 3D preview
- Build `Basic3DPreview` (Three.js)
- Render from `floorplan_json`
- Simple wall extrusion + orbit controls
- Add tabs: JSON / SVG / 3D

---

### Day 7: Polish + optional render
- Generate render prompt from final design (implemented)
- Optional image generation job (not implemented; `render_image_path` remains optional)
- Improve loading + error states
- Test with 3–5 Vietnamese house prompts
- Fix obvious UX issues only (no refactor)

## Definition of Done

Current slice is done when:

- The user can submit a Vietnamese requirement from the frontend.
- Backend creates a project.
- Ollama returns parseable `DesignBrief` JSON.
- Backend validates site dimensions.
- Backend saves the design brief and AI call log.
- Frontend displays project id, status, and extracted fields.
- Failures return visible errors.

Full MVP is done when:

- Java generates rule warnings.
- Java generates a layout plan.
- Java generates floorplan geometry.
- Java renders SVG from geometry.
- React displays the brief, warnings, SVG, simple 3D preview, and render prompt.
- AI never generates final coordinates.

## More Documentation

- Backend details: [backend/READMEbackend.md](backend/READMEbackend.md)
- Frontend details: [frontend/READMEfrontend.md](frontend/READMEfrontend.md)
