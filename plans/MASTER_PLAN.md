# Master Plan — 7 Phase Build

Reference `docs/ARCHITECTURE.md`, `docs/API_SPEC.md`, and `docs/TESTING.md` throughout. Each phase should end with a working commit — don't leave the repo in a broken state overnight.

## Phase 1 — Core Services + DB
- [ ] Scaffold Order Service (Spring Boot) with `POST/GET /orders` per `API_SPEC.md`
- [ ] Scaffold Inventory Service (FastAPI) with `GET /products`, `PATCH /products/{id}/stock`
- [ ] PostgreSQL schema for both, per `ARCHITECTURE.md` data model
- [ ] Manually verify both services with Postman
- [ ] Commit: `feat: scaffold order and inventory services with basic CRUD`

## Phase 2 — Frontend + Kafka
- [ ] Minimal React frontend: product list, place order form, order status view
- [ ] Wire Kafka: Order Service publishes `order.placed`, Inventory Service consumes it
- [ ] Add Redis caching to `GET /products`
- [ ] Commit: `feat: add frontend and Kafka event-driven inventory updates`

## Phase 3 — API Test Automation
- [ ] REST Assured suite for Order Service (`tests/api-java/`)
- [ ] Pytest suite for Inventory Service (`tests/api-python/`)
- [ ] Postman smoke collection (`tests/postman/`)
- [ ] Every endpoint: happy path + negative + schema validation, per `TESTING.md`
- [ ] Commit: `test: add API test automation for order and inventory services`

## Phase 4 — UI + Mobile Automation
- [ ] Selenium + TestNG suite with Page Object Model (`tests/ui/selenium/`)
- [ ] Playwright suite for cross-browser (`tests/ui/playwright/`)
- [ ] Appium smoke tests if time allows (`tests/mobile/`)
- [ ] Commit: `test: add UI and mobile automation suites`

## Phase 5 — Performance + Containerization
- [ ] JMeter load test for `POST /orders`, `GET /products` (`tests/performance/`)
- [ ] Dockerfile for each service + Docker Compose for full stack
- [ ] Kubernetes manifests (`k8s/`) — basic Deployment + Service YAML per component
- [ ] Verify `docker-compose up --build` works end-to-end
- [ ] Commit: `feat: add performance tests and containerize full stack`

## Phase 6 — CI/CD + Reporting + Observability
- [ ] GitHub Actions workflow: build → unit → API → integration → UI → perf smoke
- [ ] SonarCloud static analysis step
- [ ] Allure report generation + publish to GitHub Pages
- [ ] Prometheus + Grafana basic dashboard (API latency) if time allows
- [ ] Commit: `ci: add full CI/CD pipeline with static analysis and reporting`

## Phase 7 — Polish + Measurement + Interview Prep
- [ ] Time manual vs automated regression run — get a real percentage for the resume
- [ ] Finalize `README.md` with architecture diagram, setup steps, screenshots
- [ ] Review every design decision — be ready to explain each tool choice in an interview
- [ ] Tag release: `v1.0`

## Cut List (if behind schedule, drop in this order)
1. Prometheus/Grafana dashboard
2. Kubernetes manifests (Docker Compose alone is enough)
3. Appium mobile tests
4. Playwright (Selenium alone still covers UI automation)
5. SonarCloud (nice-to-have, not core)

Do not cut: API tests, CI/CD pipeline, Docker Compose — these are the non-negotiable core.
