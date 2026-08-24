# API Specification

## Order Service (`Java/Spring Boot`) — base path `/api/orders`

### POST /orders
Create a new order.

**Request:**
```json
{
  "productId": "uuid",
  "quantity": 2
}
```

**Response `201 Created`:**
```json
{
  "id": "uuid",
  "productId": "uuid",
  "quantity": 2,
  "status": "PENDING",
  "createdAt": "2026-08-22T10:00:00Z"
}
```

**Errors:**
- `400` — invalid quantity (≤ 0) or missing productId
- `404` — productId not found (validated via Inventory Service call)

### GET /orders/{id}
Returns single order. `404` if not found.

### GET /orders
Returns list of all orders. Supports `?status=` query filter.

---

## Inventory Service (`Python/FastAPI`) — base path `/api/products`

### GET /products
Returns list of all products with current stock.

### GET /products/{id}
Returns single product. `404` if not found.

### PATCH /products/{id}/stock
**Internal use only** — called by the Kafka consumer, not the frontend.

**Request:**
```json
{
  "delta": -2
}
```

**Response `200 OK`:**
```json
{
  "id": "uuid",
  "stock": 8
}
```

**Errors:**
- `409` — resulting stock would go negative

---

## Kafka Event Contract

**Topic:** `order.placed`

**Payload:**
```json
{
  "orderId": "uuid",
  "productId": "uuid",
  "quantity": 2,
  "timestamp": "2026-08-22T10:00:00Z"
}
```

**Producer:** Order Service, on successful `POST /orders`
**Consumer:** Inventory Service, triggers `PATCH /products/{id}/stock` with `delta = -quantity`

---

## Notes for Test Automation

- All endpoints above must have: 1 happy-path test, 1-2 negative/edge-case tests, and 1 schema validation test (REST Assured `JsonSchemaValidator` / Pytest `jsonschema`)
- Kafka event flow should have at least one integration test verifying: order created → event published → inventory decremented (can use Testcontainers for Kafka in tests)
