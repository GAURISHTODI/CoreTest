# Architecture & Requirements

## 1. Overview

CoreTest simulates a small e-commerce order/inventory system, built to showcase full-stack SDET capability: real services to test, and a real automation framework around them.

## 2. Functional Requirements

- User can browse products (frontend → Inventory Service)
- User can place an order (frontend → Order Service)
- Placing an order emits a `order.placed` Kafka event
- Inventory Service consumes the event and decrements stock
- User can view order status
- System exposes REST APIs for both services, documented in `API_SPEC.md`

## 3. Non-Functional Requirements

- API response time < 300ms under normal load (validated via JMeter)
- All critical paths (place order, check inventory) covered by automated tests at API + UI level
- CI pipeline must run full test suite in under 10 minutes
- Containerized — entire stack must start with `docker-compose up`

## 4. System Design

```
React Frontend
     │
     ├──────▶ Order Service (Java/Spring Boot) ──▶ PostgreSQL (orders db)
     │                  │
     │                  ▼
     │              Kafka topic: order.placed
     │                  │
     │                  ▼
     └──────▶ Inventory Service (Python/FastAPI) ──▶ PostgreSQL (inventory db)
                        │
                        ▼
                     Redis (cache: product stock lookups)
```

### Order Service (Java/Spring Boot)
- `POST /orders` — create order
- `GET /orders/{id}` — get order status
- `GET /orders` — list orders
- Publishes `order.placed` event to Kafka on successful order creation

### Inventory Service (Python/FastAPI)
- `GET /products` — list products + stock
- `GET /products/{id}` — get single product
- `PATCH /products/{id}/stock` — internal endpoint, used by Kafka consumer
- Consumes `order.placed` events, decrements stock, caches lookups in Redis

## 5. Key Design Decisions (lightweight ADRs)

**Why two languages (Java + Python) instead of one?**
Deliberate — demonstrates cross-language API testing (REST Assured + Pytest) and mirrors Apple's actual polyglot enterprise environment, where SDETs must test services regardless of implementation language.

**Why Kafka instead of direct REST calls between services?**
Demonstrates async, event-driven microservices testing — a distinct and harder testing challenge than simple request/response, and a skill explicitly called out in target job postings.

**Why Redis?**
Simple caching layer for `GET /products` — demonstrates cache-aware testing (verifying cache invalidation after stock updates), a realistic enterprise testing scenario.

**Why Kubernetes manifests alongside Docker Compose?**
Docker Compose for local dev/CI simplicity; Kubernetes manifests included to demonstrate orchestration knowledge without requiring a live cluster for daily development.

## 6. Data Model (simplified)

**orders**
| column | type |
|---|---|
| id | UUID |
| product_id | UUID |
| quantity | int |
| status | enum (PENDING, CONFIRMED, FAILED) |
| created_at | timestamp |

**products**
| column | type |
|---|---|
| id | UUID |
| name | string |
| stock | int |
| price | decimal |

## 7. Security Notes (lightweight — not a production system)

- No real payment processing — out of scope
- Basic input validation on all endpoints (reject negative quantity, unknown product IDs, etc.)
- Environment secrets (DB creds, Kafka config) via `.env`, never committed — see `.env.example`
