# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.


frontend/
├── public/                     # Tài nguyên tĩnh (favicon, icons)
├── src/
│   ├── api/                    # Gọi API backend (designApi.js)
│   ├── assets/                 # Hình ảnh, font, tài nguyên tĩnh
│   ├── canvas/                 # Logic canvas 2D (dự kiến dùng Konva)
│   ├── components/             # Component dùng chung (RequirementForm)
│   ├── hooks/                  # Custom React hooks
│   ├── layouts/                # Layout template (header, footer, sidebar)
│   ├── pages/                  # Các trang: HomePage, DesignPage
│   ├── router/                 # Định tuyến React Router (AppRouter)
│   ├── services/               # Business logic phía client
│   ├── stores/                 # State management (Zustand)
│   ├── three/                  # 3D rendering với Three.js / R3F
│   ├── utils/                  # Hàm tiện ích
│   ├── App.jsx                 # Component gốc
│   ├── main.jsx                # Entry point
│   └── index.css               # Global styles (Tailwind CSS)
├── index.html
├── vite.config.js
├── package.json
└── eslint.config.js