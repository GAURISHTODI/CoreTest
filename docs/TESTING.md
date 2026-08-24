# Testing Strategy

This is the core deliverable of the project — treat this document as authoritative when generating or reviewing tests.

## Test Pyramid for This Project

```
        /\
       /UI\          Selenium + Playwright — few, high-value flows
      /------\
     / API    \       REST Assured + Pytest — the bulk of coverage
    /----------\
   / Unit Tests \     JUnit (Java) + Pytest (Python) — fast, isolated
  /--------------\
```

## 1. Unit Tests
- **Java:** JUnit 5, Mockito for mocking repository/service layers
- **Python:** Pytest, `unittest.mock` for mocking DB/Kafka calls
- Location: `services/order-service/src/test/java/...`, `services/inventory-service/tests/unit/`
- Target: every service method with business logic (not simple getters/setters)

## 2. API Tests
- **Java suite:** REST Assured, location `tests/api-java/`
  - Naming: `OrderApiTest.java`, methods like `shouldCreateOrder_whenValidRequest()`
- **Python suite:** Pytest + `requests`, location `tests/api-python/`
  - Naming: `test_inventory_api.py`, functions like `test_get_product_returns_404_for_unknown_id()`
- **Smoke suite:** Postman collection exported to `tests/postman/`, run via Newman in CI for a fast pre-check before the full suite
- Every endpoint in `API_SPEC.md` needs: happy path, at least one negative case, schema validation

## 3. UI Tests
- **Selenium + TestNG**, location `tests/ui/selenium/`
  - Page Object Model — one class per page (`ProductListPage.java`, `CheckoutPage.java`)
  - Naming: `OrderFlowTest.java`
- **Playwright**, location `tests/ui/playwright/`
  - Used for cross-browser regression of the same core flows (place order, view order status)
- Target flows: browse products → place order → view order confirmation → verify stock decremented

## 4. Mobile Tests
- **Appium**, location `tests/mobile/`
- Minimum: 2-3 smoke tests on the mobile-wrapped frontend (launch app, place order, verify confirmation)

## 5. Performance Tests
- **JMeter**, location `tests/performance/`
- Target: `POST /orders` and `GET /products` under concurrent load (start with 50 concurrent users, ramp up)
- Capture: response time, throughput, error rate — export report to `tests/performance/reports/`

## 6. Integration Tests
- Verify the full async flow: order created → Kafka event → inventory updated
- Use Testcontainers (Kafka + Postgres) so this runs reliably in CI, not against shared infra

## 7. CI Test Execution Order

```
Unit Tests → API Tests (Postman smoke, then full REST Assured + Pytest) → 
Integration Tests → UI Tests (headless) → Performance Smoke Test
```

Fail fast — if unit tests fail, don't proceed to slower suites.

## 8. Reporting

- All suites output to a common format collected by **Allure**
- CI publishes the Allure report as a GitHub Pages artifact after each run
- Every test run should produce: pass/fail count, duration, and (for perf tests) latency percentiles

## 9. What "Done" Looks Like for a Test

A test is not complete until it:
1. Has a clear, descriptive name stating what it verifies
2. Asserts on specific expected values, not just "no exception thrown"
3. Cleans up any data it creates (no test pollution across runs)
4. Runs successfully in CI, not just locally
