# AGENTS.md — Instructions for AI Coding Assistants

This file orients any AI assistant (Claude Code, Cursor, Copilot, etc.) working on this repo. Read this first, then `docs/ARCHITECTURE.md` and `docs/TESTING.md` before writing code.

## Project

**CoreTest** — a microservices e-commerce platform built specifically to demonstrate SDET (Software Development Engineer in Test) skills: backend development, multi-layer test automation, CI/CD, containerization, and observability. This is a portfolio/interview project, not a production system — prioritize clarity and demonstrable best practices over premature optimization.

## Goals (in priority order)

1. Two working backend services (Order + Inventory) that talk to each other via REST and Kafka
2. A genuinely well-structured automated test suite (API + UI + performance) — this is the core deliverable, not an afterthought
3. A working CI/CD pipeline that runs the whole test suite on every push
4. Clean, well-documented code that the author can explain line-by-line in a technical interview

## Non-goals

- Do not over-engineer. No need for advanced scaling, multi-region deployment, or production-grade security hardening.
- Do not add a tool/framework unless it's used meaningfully — every tool listed in the tech stack must have real, working code behind it.
- Do not silently skip failing tests or use `@Ignore`/`skip` to make CI green — fix or clearly flag.

## Tech Stack (source of truth)

- **Order Service:** Java 17, Spring Boot, Maven, PostgreSQL
- **Inventory Service:** Python 3.11, FastAPI, PostgreSQL
- **Frontend:** React
- **Messaging:** Kafka (order.placed event → inventory update)
- **Cache:** Redis
- **API Tests:** REST Assured (Java), Pytest + Requests (Python), Postman/Newman (smoke)
- **UI Tests:** Selenium + TestNG (Page Object Model), Playwright
- **Mobile Tests:** Appium
- **Performance:** JMeter
- **Static Analysis:** SonarCloud
- **CI/CD:** GitHub Actions
- **Containerization:** Docker, Docker Compose, Kubernetes manifests
- **Reporting:** Allure Report
- **Observability:** Prometheus + Grafana

## Code Conventions

- Java: standard Spring Boot layout (`controller/`, `service/`, `repository/`, `model/`), Lombok allowed
- Python: FastAPI standard layout (`routers/`, `services/`, `models/`, `schemas/`), type hints required
- Commit style: Conventional Commits (`feat:`, `fix:`, `test:`, `chore:`, `docs:`)
- Branching: `feature/<name>`, PR into `main`, no direct pushes to `main` once CI is live

## Working Order (see `plans/MASTER_PLAN.md` for full detail)

1. Core services + DB schema
2. Frontend + Kafka event wiring
3. API test automation
4. UI + mobile test automation
5. Performance testing + containerization
6. CI/CD + static analysis + reporting
7. Observability + polish + README

## When Generating Code

- Always check `docs/API_SPEC.md` before implementing or testing an endpoint — don't invent endpoint shapes
- Always check `docs/TESTING.md` for the expected test structure and naming conventions before adding tests
- If a requirement is ambiguous, prefer the simplest implementation that's still demonstrably "correct" — this project will be explained verbally in interviews, so simple-but-real beats clever-but-fragile
- Every new feature should ship with at least one corresponding test in the same PR/commit
