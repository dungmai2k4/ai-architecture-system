# ArchitectAI Frontend

React + Vite frontend for the ArchitectAI MVP.

The current frontend implements one ChatGPT-style design workspace: a dark fixed-height sidebar, internally scrollable conversation area, bottom composer, and assistant result card that calls the backend and displays the generated design output.

The screen displays extracted briefs, rule warnings, layout data, SVG floorplans, a simple Three.js preview, and a render prompt. Routing, polling, and Konva canvas rendering are not implemented yet.

## React Architecture

```text
main.jsx
  |
  v
App.jsx
  |
  v
DesignPage
  |
  +-- Chat-style sidebar / message shell
  +-- RequirementForm
  +-- ResultPanel
```

The app uses local React state only. There is no global store, router setup, query client, or custom API layer in the implemented workflow.

## Vite Setup

Vite config:

```text
vite.config.js
```

Enabled plugins:

- `@vitejs/plugin-react`
- `@tailwindcss/vite`

Global CSS:

```text
src/index.css
```

Current content:

```css
@import "tailwindcss";
```

## Folder Structure

```text
frontend/
+-- package.json                  # React, Vite, Tailwind, 3D/canvas dependencies
+-- package-lock.json             # Locked npm dependency tree
+-- vite.config.js                # React and Tailwind Vite plugins
+-- eslint.config.js              # ESLint flat config
+-- index.html                    # Vite HTML entry
+-- public/
|   +-- favicon.svg               # Browser icon
|   +-- icons.svg                 # Static icon asset
+-- src/
    +-- main.jsx                  # Creates React root
    +-- App.jsx                   # Renders DesignPage directly
    +-- index.css                 # Tailwind import
    +-- features/design/
    |   +-- DesignPage.jsx        # Chat-style shell, API call, loading/error/result state
    |   +-- RequirementForm.jsx   # Bottom chat composer and submit button
    |   +-- ResultPanel.jsx       # Assistant result card with brief, layout, SVG, JSON, 3D, render prompt
    +-- components/
        +-- Sidebar.jsx           # Chat sidebar mock component, unused
        +-- ChatMessage.jsx       # Chat bubble component, unused
        +-- ChatInput.jsx         # Auto-resizing chat input, unused
```

## Routing

There is no active routing.

`react-router-dom` is installed, but `App.jsx` directly renders:

```jsx
<DesignPage />
```

No routes, layouts, or page router files exist in `src/`.

## State Management

`DesignPage.jsx` uses local `useState` values:

- `requirement`
- `result`
- `error`
- `loading`
- `submittedRequirement`

There is no Redux, Zustand, Context state layer, or server-state library.

## API Integration

The implemented API call is inside `DesignPage.jsx`.

Endpoint:

```text
POST http://localhost:8080/api/designs
```

Request body:

```json
{
  "requirement": "..."
}
```

Behavior:

- Clears previous error and result.
- Sets loading state.
- Sends JSON with `fetch`.
- Reads JSON response.
- Throws an error when `response.ok` is false.
- Displays returned backend error message when available.

There is no separate `src/api/` folder in the real code.

Axios is installed but not used by the current implementation.

## UI Workflow

Current user flow:

1. User sees a ChatGPT-style empty state with suggested Vietnamese house prompts.
2. User types a Vietnamese house design requirement in the bottom composer or picks a suggestion.
3. Frontend renders the submitted requirement as a user message and calls the backend.
4. The assistant message area shows a loading indicator while the backend generates the design.
5. If the backend fails, an assistant error message is shown.
6. If the backend succeeds, `ResultPanel` renders the generated design as an assistant artifact card.

Displayed result fields:

- Project ID
- Status
- Site width/depth
- Floors
- Bedrooms
- Bathrooms
- Style
- Orientation and layout intent
- Room relationships and floor requirements
- Requested rooms
- Preferences
- Rule warnings
- Layout guidance
- SVG/JSON/3D floorplan tabs
- Render prompt for optional image generation

## Component Structure

### `DesignPage.jsx`

Owns the active chat-style screen.

Responsibilities:

- Track composer text.
- Track the last submitted user message.
- Submit to backend.
- Track loading/error/result.
- Render sidebar suggestions, empty state, user message, assistant shell, `RequirementForm`, and `ResultPanel`.

### `RequirementForm.jsx`

Responsibilities:

- Render the fixed-bottom chat composer textarea.
- Submit with the send button or Enter, while Shift+Enter keeps a newline.
- Disable send while loading or empty.
- Pass edits and submit event to parent.

### `ResultPanel.jsx`

Responsibilities:

- Render project id and status.
- Render backend error if present.
- Render fallback text when `designBrief` is missing.
- Render extracted `DesignBrief` fields.
- Render layout guidance, warnings, SVG/JSON tabs, Three.js preview, and render prompt.

### Unused chat components

These files exist but are not imported by `App.jsx`:

- `components/Sidebar.jsx`
- `components/ChatMessage.jsx`
- `components/ChatInput.jsx`

They look like an earlier chat UI direction and should either be wired into a real workflow or removed later.

## Styling Approach

The frontend uses Tailwind CSS utility classes directly in JSX.

There is no:

- component library.
- CSS module setup.
- styled-components.
- design token file.

Current screen styling is simple and local to components. The main shell uses a `h-screen`/`overflow-hidden` layout so only the conversation panel scrolls; the desktop sidebar stays anchored while long results scroll inside the chat area.

## SVG Rendering

SVG floorplan rendering is implemented in `ResultPanel.jsx`. The backend returns SVG content inside the floorplan payload, and the frontend converts it to a data URL for display in the SVG tab.

There is still no separate `svg/` folder or dedicated backend SVG URL; the current MVP keeps the SVG inline in the API response.

## Three.js Preview

A basic 3D preview is implemented inside `ResultPanel.jsx` using:

- `three`
- `@react-three/fiber`
- `@react-three/drei`

The preview consumes backend-generated floorplan JSON, extrudes rooms/walls/openings/furniture into simple meshes, and uses orbit controls. There is no separate `three/` folder yet; extracting this into a dedicated component is a future cleanup task.

## Canvas / Konva

Konva dependencies are installed:

- `konva`
- `react-konva`

No Konva canvas component exists in the current `src/` tree.

## Polling Logic

No polling exists.

There are no async render jobs or render-status endpoints in the backend, so the frontend has nothing to poll yet.

## Local Frontend Development

Requirements:

- Node.js 18+
- Backend running on `http://localhost:8080`

Install dependencies:

```bash
npm install
```

Run dev server:

```bash
npm run dev
```

Default Vite URL:

```text
http://localhost:5173
```

Build:

```bash
npm run build
```

Lint:

```bash
npm run lint
```

Preview production build:

```bash
npm run preview
```

## Frontend Shipping Rule

Keep the frontend direct:

```text
React state -> backend API -> render backend result
```

Do not add routing, global state, polling, canvas, or 3D until the backend returns data that needs those features.
