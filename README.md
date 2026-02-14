# 🍽️ Meal Planner

A family meal planning application for organising weekly meals and shopping lists. Built with **Java Spring Boot** (backend) and **React** (frontend), designed to run on a Raspberry Pi.

## Features

- **Meal Management** — Add, edit, and delete meals with ingredients, prep/cook times, and effort levels
- **Family Members** — Track each person's eating and cooking preferences
- **Weekly Planner** — Drag-and-drop meal planning across the week, with cook assignment
- **Meal Ratings** — Rate meals after eating and view average ratings
- **Shopping List** — Auto-generated from the week's meal plan, with checkboxes and export/print

## Tech Stack

| Layer    | Technology                     |
| -------- | ------------------------------ |
| Backend  | Java 17, Spring Boot 3.2, JPA |
| Database | H2 (file-based, persistent)   |
| Frontend | React 18, Vite, React Router  |
| DnD      | @hello-pangea/dnd              |

## Prerequisites

- **Java 17+** (e.g. `brew install openjdk@17` on macOS)
- **Node.js 18+** and npm
- **Maven 3.8+** (or use the included Maven wrapper)

## Quick Start — Development

### 1. Start the backend

```bash
cd backend
./mvnw spring-boot:run
```

The API will be running at `http://localhost:8080`.

### 2. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The UI will be at `http://localhost:3000` with API requests proxied to the backend.

## Production Build (Raspberry Pi)

Build everything into a single JAR:

```bash
chmod +x build.sh
./build.sh
```

Then run on your Raspberry Pi:

```bash
java -jar backend/target/meal-planner-1.0.0.jar
```

Open `http://<raspberry-pi-ip>:8080` in your browser.

### Run as a service (systemd)

Create `/etc/systemd/system/meal-planner.service`:

```ini
[Unit]
Description=Meal Planner
After=network.target

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/meal-planner/backend
ExecStart=/usr/bin/java -jar target/meal-planner-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Then:

```bash
sudo systemctl enable meal-planner
sudo systemctl start meal-planner
```

## Testing

### Run all tests

Run both backend and frontend test suites with a single command from the project root:

```bash
chmod +x test.sh
./test.sh
```

### Backend tests only

```bash
cd backend
./mvnw test
```

107 tests across unit and controller layers (JUnit 5, Mockito, MockMvc).

### Frontend tests only

```bash
cd frontend
npm test
```

62 tests covering the API client, components, and routing (Vitest, React Testing Library).

### Continuous Integration

A GitHub Actions workflow (`.github/workflows/tests.yml`) runs both test suites automatically on every push and pull request to `main`. Backend and frontend jobs run in parallel.

## API Reference

### Meals

| Method | Endpoint             | Description      |
| ------ | -------------------- | ---------------- |
| GET    | `/api/meals`         | List all meals   |
| GET    | `/api/meals?search=` | Search meals     |
| GET    | `/api/meals/{id}`    | Get meal by ID   |
| POST   | `/api/meals`         | Create a meal    |
| PUT    | `/api/meals/{id}`    | Update a meal    |
| DELETE | `/api/meals/{id}`    | Delete a meal    |

### People

| Method | Endpoint           | Description         |
| ------ | ------------------ | ------------------- |
| GET    | `/api/people`      | List all people     |
| POST   | `/api/people`      | Add a person        |
| PUT    | `/api/people/{id}` | Update a person     |
| DELETE | `/api/people/{id}` | Remove a person     |

### Meal Plans

| Method | Endpoint                             | Description             |
| ------ | ------------------------------------ | ----------------------- |
| GET    | `/api/meal-plans/week?date=`         | Get/create week plan    |
| POST   | `/api/meal-plans/{id}/entries`       | Add entry to plan       |
| PUT    | `/api/meal-plans/entries/{entryId}`  | Update entry            |
| DELETE | `/api/meal-plans/entries/{entryId}`  | Remove entry            |
| GET    | `/api/meal-plans/{id}/shopping-list` | Generate shopping list  |

### Ratings

| Method | Endpoint                          | Description         |
| ------ | --------------------------------- | ------------------- |
| GET    | `/api/ratings/meal/{mealId}`      | Ratings for a meal  |
| GET    | `/api/ratings/meal/{id}/average`  | Average rating      |
| POST   | `/api/ratings`                    | Submit a rating     |

## Database

Uses **H2** with file-based storage at `backend/data/mealplanner.mv.db`. Data persists across restarts. The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/mealplanner`).

## Project Structure

```
meal-planner/
├── backend/                  # Spring Boot application
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/mealplanner/
│       │   ├── config/           # CORS, SPA routing
│       │   ├── controller/       # REST controllers
│       │   ├── dto/              # Request/response DTOs
│       │   ├── model/            # JPA entities
│       │   ├── repository/       # Data repositories
│       │   └── service/          # Business logic
│       └── test/java/com/mealplanner/
│           ├── controller/       # MockMvc controller tests
│           ├── dto/              # DTO unit tests
│           ├── model/            # Entity & enum tests
│           └── service/          # Service unit tests
├── frontend/                 # React application
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api.js            # API client
│       ├── __tests__/        # Vitest test suites
│       ├── components/       # Reusable components
│       └── pages/            # Page components
├── .github/workflows/        # CI — runs tests on push/PR
├── build.sh                  # Production build script
├── test.sh                   # Run all tests (backend + frontend)
└── requirements.md           # Project requirements
```
