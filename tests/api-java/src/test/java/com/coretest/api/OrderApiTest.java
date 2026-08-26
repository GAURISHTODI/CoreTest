package com.coretest.api;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 * REST Assured API tests for the Order Service.
 *
 * Covers every endpoint in API_SPEC.md with:
 *   - Happy path tests
 *   - Negative/edge-case tests
 *   - JSON schema validation tests
 *
 * Naming convention per TESTING.md:
 *   shouldDoSomething_whenCondition()
 *
 * Prerequisites: Order Service running on localhost:8080,
 *                Inventory Service running on localhost:8000 (for valid product IDs).
 */
@Feature("Order Service API")
public class OrderApiTest {

    /**
     * A valid product ID that exists in the Inventory Service seed data.
     * This is set dynamically in @BeforeClass by querying the Inventory Service.
     */
    private String validProductId;

    /**
     * An order ID created during the happy-path POST test,
     * reused by GET tests to avoid coupling to external state.
     */
    private String createdOrderId;

    @BeforeClass
    public void setup() {
        // Order Service base URI
        RestAssured.baseURI = System.getProperty("order.service.url", "http://localhost:8080");

        // Fetch a valid product ID from the Inventory Service
        String inventoryBaseUrl = System.getProperty("inventory.service.url", "http://localhost:8000");
        validProductId = given()
                .baseUri(inventoryBaseUrl)
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("[0].id");

        // Guard: fail fast if no products are seeded
        if (validProductId == null || validProductId.isEmpty()) {
            throw new IllegalStateException(
                    "No products found in Inventory Service — ensure seed data is loaded before running tests");
        }
    }

    // ========================================================================
    // POST /api/orders — Happy Path
    // ========================================================================

    @Test(priority = 1)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Create a new order with a valid product ID and quantity — expect 201 PENDING")
    public void shouldCreateOrder_whenValidRequest() {
        String requestBody = String.format(
                "{\"productId\":\"%s\", \"quantity\":2}", validProductId);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .body("status", equalTo("PENDING"))
                .body("productId", equalTo(validProductId))
                .body("quantity", equalTo(2))
                .body("id", notNullValue())
                .body("createdAt", notNullValue())
                .extract()
                .response();

        // Save the created order ID for subsequent GET tests
        createdOrderId = response.jsonPath().getString("id");
    }

    // ========================================================================
    // POST /api/orders — Negative Cases
    // ========================================================================

    @Test(priority = 2)
    @Severity(SeverityLevel.NORMAL)
    @Description("Reject order when quantity is zero — expect 400")
    public void shouldReturn400_whenQuantityIsZero() {
        String requestBody = String.format(
                "{\"productId\":\"%s\", \"quantity\":0}", validProductId);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", notNullValue());
    }

    @Test(priority = 2)
    @Severity(SeverityLevel.NORMAL)
    @Description("Reject order when productId is missing — expect 400")
    public void shouldReturn400_whenProductIdMissing() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"quantity\":1}")
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", notNullValue());
    }

    // ========================================================================
    // POST /api/orders — Schema Validation
    // ========================================================================

    @Test(priority = 3, dependsOnMethods = "shouldCreateOrder_whenValidRequest")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify POST /api/orders response matches the JSON schema contract")
    public void shouldMatchCreateOrderJsonSchema() {
        String requestBody = String.format(
                "{\"productId\":\"%s\", \"quantity\":1}", validProductId);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/create-order-response.json"));
    }

    // ========================================================================
    // GET /api/orders/{id} — Happy Path
    // ========================================================================

    @Test(priority = 4, dependsOnMethods = "shouldCreateOrder_whenValidRequest")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Retrieve an existing order by ID — expect 200 with correct data")
    public void shouldReturnOrder_whenValidId() {
        given()
                .when()
                .get("/api/orders/{id}", createdOrderId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdOrderId))
                .body("productId", equalTo(validProductId))
                .body("quantity", equalTo(2))
                .body("status", equalTo("PENDING"));
    }

    // ========================================================================
    // GET /api/orders/{id} — Negative Cases
    // ========================================================================

    @Test(priority = 4)
    @Severity(SeverityLevel.NORMAL)
    @Description("Request a non-existent order — expect 404")
    public void shouldReturn404_whenOrderNotFound() {
        String fakeId = UUID.randomUUID().toString();

        given()
                .when()
                .get("/api/orders/{id}", fakeId)
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("message", containsString(fakeId));
    }

    // ========================================================================
    // GET /api/orders/{id} — Schema Validation
    // ========================================================================

    @Test(priority = 5, dependsOnMethods = "shouldReturnOrder_whenValidId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /api/orders/{id} response matches the JSON schema contract")
    public void shouldMatchGetOrderJsonSchema() {
        given()
                .when()
                .get("/api/orders/{id}", createdOrderId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/get-order-response.json"));
    }

    // ========================================================================
    // GET /api/orders — Happy Path (List)
    // ========================================================================

    @Test(priority = 6, dependsOnMethods = "shouldCreateOrder_whenValidRequest")
    @Severity(SeverityLevel.CRITICAL)
    @Description("List all orders — expect 200 with non-empty array")
    public void shouldReturnAllOrders() {
        given()
                .when()
                .get("/api/orders")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].id", notNullValue())
                .body("[0].status", notNullValue());
    }

    @Test(priority = 7, dependsOnMethods = "shouldCreateOrder_whenValidRequest")
    @Severity(SeverityLevel.NORMAL)
    @Description("Filter orders by status=PENDING — expect 200 with only PENDING orders")
    public void shouldFilterOrdersByStatus() {
        given()
                .queryParam("status", "PENDING")
                .when()
                .get("/api/orders")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("status", everyItem(equalTo("PENDING")));
    }
}
