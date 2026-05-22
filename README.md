# ArchitectAI — AI Architecture Design System

Hệ thống thiết kế nhà ở và công trình sử dụng AI, xây dựng với Frontend React và Backend Java Spring Boot.

## Mô tả ngắn

ArchitectAI cho phép người dùng nhập yêu cầu thiết kế nhà (diện tích đất, số tầng, phong cách, mô tả chi tiết), sau đó hệ thống AI sẽ trích xuất thông số, lập kế hoạch mặt bằng và tạo bản vẽ 3D/2D tương ứng.

## Cấu trúc thư mục

### Frontend (`frontend/`)

```
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
```

### Backend (`backend/`)

```
backend/
├── src/main/java/com/architectai/
│   ├── ai/                     # Module AI / NLP xử lý ngôn ngữ
│   ├── config/                 # Cấu hình (CORS, Security,...)
│   ├── controller/             # REST API controllers
│   ├── dto/                    # Data Transfer Objects
│   ├── entity/                 # JPA entities (MySQL)
│   ├── planner/                # Logic lập kế hoạch mặt bằng
│   ├── render/                 # Module render bản vẽ
│   ├── repository/             # Spring Data JPA repositories
│   ├── service/                # Business logic services
│   └── ArchitectAiBackendApplication.java  # Main class
├── src/main/resources/
│   └── application.properties  # Cấu hình datasource, JPA, server
├── src/test/                   # Unit / integration tests
├── pom.xml                     # Maven dependencies
└── HELP.md                     # Tài liệu Spring Boot mặc định
```

## Dependencies chính

### Frontend

| Package | Mục đích |
|---------|----------|
| `react` / `react-dom` | Thư viện UI core (React 19) |
| `react-router-dom` | Định tuyến client-side |
| `axios` | HTTP client gọi API backend |
| `tailwindcss` | Utility-first CSS framework |
| `@tailwindcss/vite` | Tailwind CSS Vite plugin |
| `three` | WebGL 3D rendering engine |
| `@react-three/fiber` | React renderer cho Three.js |
| `@react-three/drei` | Tiện ích bổ sung cho R3F |
| `konva` / `react-konva` | Canvas 2D cho bản vẽ mặt bằng |
| `lucide-react` | Icon set |
| `zustand` (trong lockfile) | State management nhẹ |
| `vite` | Build tool & dev server |
| `eslint` | Linting & code quality |

### Backend

| Package | Mục đích |
|---------|----------|
| `spring-boot-starter-webmvc` | REST API với Spring MVC |
| `spring-boot-starter-data-jpa` | ORM & database access (JPA/Hibernate) |
| `spring-boot-starter-validation` | Bean validation (`@Valid`, `@NotBlank`,...) |
| `mysql-connector-j` | MySQL JDBC driver |
| `lombok` | Giảm boilerplate code (`@Data`, `@Builder`,...) |
| `spring-boot-starter-validation-test` | Test hỗ trợ validation |
| `spring-boot-starter-webmvc-test` | Test hỗ trợ web MVC |

## Hướng dẫn cài đặt

### Yêu cầu

- Node.js >= 18
- Java 21+
- Maven (hoặc dùng `mvnw` có sẵn)
- MySQL 8+

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend chạy tại `http://localhost:5173`.

### Backend

1. Tạo database MySQL:

```sql
CREATE DATABASE architect_ai;
```

2. Cấu hình kết nối trong `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/architect_ai
spring.datasource.username=root
spring.datasource.password=your_password
```

3. Chạy backend:

```bash
cd backend
mvnw spring-boot:run
```

Backend chạy tại `http://localhost:8080`.

## Ví dụ sử dụng

### Gửi yêu cầu thiết kế

```bash
curl -X POST http://localhost:8080/api/design/extract \
  -H "Content-Type: application/json" \
  -d '{
    "landSize": "5x20m",
    "floors": 2,
    "style": "modern",
    "requirements": "3 phòng ngủ, 2 WC, phòng khách rộng"
  }'
```

### Danh sách API

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/design/extract` | Trích xuất thông số từ yêu cầu |
| POST | `/api/design/plan` | Tạo mặt bằng sơ bộ |
| POST | `/api/design/render` | Render bản vẽ 3D/2D |

## License

MIT
