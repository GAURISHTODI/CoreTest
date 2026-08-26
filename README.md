# CoreTest

**SDET Automation Framework for a Microservices E-Commerce Platform**

CoreTest is a production-style microservices application paired with a full-stack automated test framework — covering API, UI, mobile, and performance testing, wired into a CI/CD pipeline with observability and reporting.

---

## Architecture

```
┌─────────────┐      ┌──────────────────┐      ┌───────────────────┐
│   React     │─────▶│  Order Service    │─────▶│  Inventory Service │
│  Frontend   │      │ (Java/Spring Boot)│      │  (Python/FastAPI)  │
└─────────────┘      └────────┬──────────┘      └──────────┬─────────┘
                               │                             │
                          ┌────▼────┐                   ┌────▼────┐
                          │PostgreSQL│                   │PostgreSQL│
                          └─────────┘                   └─────────┘
                               │                             ▲
                               └──────────Kafka──────────────┘
                                  (order.placed event)

                          ┌─────────┐
                          │  Redis  │  (caching layer)
                          └─────────┘
```

- **Order Service** (Java, Spring Boot) — handles order creation, tracking, and status updates
- **Inventory Service** (Python, FastAPI) — manages stock levels, consumes order events via Kafka
- **Frontend** (React) — browse products, place orders
- **Redis** — caching layer for frequently accessed inventory data
- **PostgreSQL** — persistence for both services

---

## Test Automation

| Layer | Frameworks |
|---|---|
| API Testing | REST Assured (Java), Pytest + Requests (Python), Postman/Newman (smoke) |
| UI Automation | Selenium + TestNG (Page Object Model), Playwright |
| Mobile | Appium |
| Performance | JMeter |
| Static Analysis | SonarCloud |
| Reporting | Allure Report |
| Observability | Prometheus + Grafana |

---

## CI/CD Pipeline

GitHub Actions runs on every push:

```
Build → Unit Tests → API Tests → UI Tests → Performance Smoke Test → Static Analysis → Publish Reports
```

All services are containerized with **Docker** and orchestrated locally via **Docker Compose**; Kubernetes manifests are included for cluster deployment.

---

## Tech Stack

Java, Spring Boot, Kafka, Docker, Kubernetes, Selenium, Playwright, REST Assured, Appium, GitHub Actions, Python, FastAPI, PostgreSQL, Redis, JMeter, TestNG, Pytest, Allure, SonarCloud, Prometheus, Grafana, React, Postman, Docker Compose, Maven, Git

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17+, Maven
- Python 3.11+
- Node.js 18+


### Run the Application
Start the full stack (Order Service, Inventory Service, Frontend, DBs, Kafka, Redis, Prometheus, Grafana) locally using Docker Compose:
```bash
docker compose up --build -d
```
- Frontend: `http://localhost:5173`
- Order API: `http://localhost:8080/api/orders`
- Inventory API: `http://localhost:8000/api/products`
- Grafana Dashboard: `http://localhost:3000` (admin/admin)
- Prometheus: `http://localhost:9090`

### Run Tests
```bash
# Postman Smoke Tests
npx newman run tests/postman/CoreTest_Smoke.postman_collection.json -e tests/postman/environment.json

# API tests (Java)
cd tests/api-java && mvn test

# API tests (Python)
cd tests/api-python && pytest

# UI tests (Selenium)
cd tests/ui/selenium && mvn test

# UI tests (Playwright)
cd tests/ui/playwright && npx playwright test

# Mobile tests (Appium)
cd tests/mobile && mvn test

# Performance tests
cd tests/performance && jmeter -n -t CoreTest_LoadPlan.jmx
```

Test reports are generated via Allure and published automatically by the CI pipeline.

---

## Project Structure

```
coretest/
├── services/
│   ├── order-service/        # Java Spring Boot
│   └── inventory-service/    # Python FastAPI
├── frontend/                 # React app
├── tests/
│   ├── api-java/             # REST Assured
│   ├── api-python/           # Pytest
│   ├── ui/                   # Selenium + Playwright
│   ├── mobile/               # Appium
│   └── performance/          # JMeter
├── k8s/                      # Kubernetes manifests
├── .github/workflows/        # CI/CD pipelines
└── docker-compose.yml
```

---

## Roadmap
- [x] Core services + database layer
- [x] Kafka event-driven communication
- [x] API test automation
- [x] UI + mobile automation
- [x] CI/CD pipeline with reporting
- [x] Full observability dashboard (Prometheus/Grafana)
- [x] Containerization and Kubernetes deployment manifests

---

## Author
Gaurish Todi — Built as part of SDET internship preparation.