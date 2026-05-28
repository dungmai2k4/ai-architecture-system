# ArchitectAI Frontend

React + Vite frontend for the ArchitectAI MVP.

The current frontend implements one screen: a requirement form that calls the backend and displays the extracted `DesignBrief`.

The screen now displays extracted briefs, rule warnings, layout data, SVG floorplans, a simple Three.js preview, and a render prompt. Routing, polling, and canvas rendering are not implemented yet.

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
    |   +-- DesignPage.jsx        # Active screen, API call, loading/error/result state
    |   +-- RequirementForm.jsx   # Textarea and submit button
    |   +-- ResultPanel.jsx       # Displays project id, status, DesignBrief fields
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

1. User types a Vietnamese house design requirement.
2. User submits the form.
3. Frontend calls the backend.
4. Button shows loading text.
5. If the backend fails, an error box is shown.
6. If the backend succeeds, `ResultPanel` renders the extracted brief.

Displayed result fields:

- Project ID
- Status
- Site width/depth
- Floors
- Bedrooms
- Bathrooms
- Style
- Requested rooms
- Preferences
- Render prompt for optional image generation

Note: some Vietnamese strings in current source files appear mojibake-encoded. This is a text encoding cleanup task.

## Component Structure

### `DesignPage.jsx`

Owns the active screen.

Responsibilities:

- Track form text.
- Submit to backend.
- Track loading/error/result.
- Render `RequirementForm`.
- Render `ResultPanel`.

### `RequirementForm.jsx`

Responsibilities:

- Render textarea.
- Render submit button.
- Disable button while loading.
- Pass edits and submit event to parent.

### `ResultPanel.jsx`

Responsibilities:

- Render project id and status.
- Render backend error if present.
- Render fallback text when `designBrief` is missing.
- Render extracted `DesignBrief` fields.

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

Current screen styling is simple and local to components.

## SVG Rendering

SVG floorplan rendering is not implemented.

There is no:

- SVG viewer component.
- `svg/` folder.
- `canvas/` floorplan viewer.
- backend SVG URL displayed in the UI.

The current frontend only displays extracted JSON fields as text.

## Three.js Preview

3D preview is not implemented.

Installed packages:

- `three`
- `@react-three/fiber`
- `@react-three/drei`

Missing from current source:

- `three/` folder.
- `Basic3DPreview.jsx`.
- canvas scene.
- wall extrusion logic.
- orbit controls usage.

When implemented, the 3D preview should consume backend-generated floorplan JSON. It should not invent geometry in the frontend.

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
