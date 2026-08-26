"""
API tests for the Inventory Service (FastAPI).

Covers every endpoint in API_SPEC.md with:
  - Happy path tests
  - Negative/edge-case tests
  - JSON schema validation tests

Naming convention per TESTING.md:
  test_<what_it_does>()

Prerequisites: Inventory Service running on localhost:8000 with seed data.
"""

import json
import uuid
from pathlib import Path

import allure
import pytest
import requests
from jsonschema import validate

# Load JSON schemas once at module level
SCHEMAS_DIR = Path(__file__).parent / "schemas"

with open(SCHEMAS_DIR / "product_response.json") as f:
    PRODUCT_SCHEMA = json.load(f)

with open(SCHEMAS_DIR / "product_list_response.json") as f:
    PRODUCT_LIST_SCHEMA = json.load(f)


# ============================================================================
# GET /api/products — Happy Path
# ============================================================================


@allure.feature("Inventory Service API")
@allure.severity(allure.severity_level.CRITICAL)
@allure.description("List all products — expect 200 with non-empty array")
def test_get_all_products(base_url: str):
    response = requests.get(f"{base_url}/api/products")

    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) > 0, "Expected at least one seeded product"

    # Verify each item has the required fields
    for product in data:
        assert "id" in product
        assert "name" in product
        assert "stock" in product
        assert "price" in product


# ============================================================================
# GET /api/products — Schema Validation
# ============================================================================


@allure.feature("Inventory Service API")
@allure.severity(allure.severity_level.NORMAL)
@allure.description("Verify GET /api/products response matches the JSON schema")
def test_get_all_products_returns_list_schema(base_url: str):
    response = requests.get(f"{base_url}/api/products")

    assert response.status_code == 200
    validate(instance=response.json(), schema=PRODUCT_LIST_SCHEMA)


# ============================================================================
# GET /api/products/{id} — Happy Path
# ============================================================================


@allure.feature("Inventory Service API")
@allure.severity(allure.severity_level.CRITICAL)
@allure.description("Retrieve a single product by ID — expect 200 with correct data")
def test_get_product_by_id(base_url: str, existing_product: dict, existing_product_id: str):
    response = requests.get(f"{base_url}/api/products/{existing_product_id}")

    assert response.status_code == 200
    data = response.json()
    assert data["id"] == existing_product_id
    assert data["name"] == existing_product["name"]
    assert isinstance(data["stock"], int)


# ============================================================================
# GET /api/products/{id} — Negative Case
# ============================================================================


@allure.feature("Inventory Service API")
@allure.severity(allure.severity_level.NORMAL)
@allure.description("Request a non-existent product — expect 404")
def test_get_product_returns_404_for_unknown_id(base_url: str):
    fake_id = str(uuid.uuid4())
    response = requests.get(f"{base_url}/api/products/{fake_id}")

    assert response.status_code == 404
    data = response.json()
    assert "detail" in data


# ============================================================================
# GET /api/products/{id} — Schema Validation
# ============================================================================


@allure.feature("Inventory Service API")
@allure.severity(allure.severity_level.NORMAL)
@allure.description("Verify GET /api/products/{id} response matches the JSON schema")
def test_get_product_response_schema(base_url: str, existing_product_id: str):
    response = requests.get(f"{base_url}/api/products/{existing_product_id}")

    assert response.status_code == 200
    validate(instance=response.json(), schema=PRODUCT_SCHEMA)


# ============================================================================
# PATCH /api/products/{id}/stock — Happy Path (Decrement)
# ============================================================================


@allure.feature("Inventory Service API")
@allure.severity(allure.severity_level.CRITICAL)
@allure.description("Decrement stock by 1 — expect 200 with updated stock")
def test_patch_stock_decrement(base_url: str, existing_product_id: str):
    # Get current stock first
    before = requests.get(f"{base_url}/api/products/{existing_product_id}").json()
    original_stock = before["stock"]

    # Decrement by 1
    response = requests.patch(
        f"{base_url}/api/products/{existing_product_id}/stock",
        json={"delta": -1},
    )

    assert response.status_code == 200
    data = response.json()
    assert data["id"] == existing_product_id
    assert data["stock"] == original_stock - 1

    # Restore: increment back by 1 so the test is idempotent
    requests.patch(
        f"{base_url}/api/products/{existing_product_id}/stock",
        json={"delta": 1},
    )


# ============================================================================
# PATCH /api/products/{id}/stock — Happy Path (Increment)
# ============================================================================


@allure.feature("Inventory Service API")
@allure.severity(allure.severity_level.NORMAL)
@allure.description("Increment stock by 5 — expect 200 with updated stock")
def test_patch_stock_increment(base_url: str, existing_product_id: str):
    before = requests.get(f"{base_url}/api/products/{existing_product_id}").json()
    original_stock = before["stock"]

    response = requests.patch(
        f"{base_url}/api/products/{existing_product_id}/stock",
        json={"delta": 5},
    )

    assert response.status_code == 200
    data = response.json()
    assert data["stock"] == original_stock + 5

    # Restore
    requests.patch(
        f"{base_url}/api/products/{existing_product_id}/stock",
        json={"delta": -5},
    )


# ============================================================================
# PATCH /api/products/{id}/stock — Negative Case (409 Conflict)
# ============================================================================


@allure.feature("Inventory Service API")
@allure.severity(allure.severity_level.NORMAL)
@allure.description("Attempt to decrement stock below zero — expect 409 Conflict")
def test_patch_stock_returns_409_when_negative(base_url: str, existing_product_id: str):
    # Try to decrement by an absurdly large number
    response = requests.patch(
        f"{base_url}/api/products/{existing_product_id}/stock",
        json={"delta": -999999},
    )

    assert response.status_code == 409
    data = response.json()
    assert "detail" in data


# ============================================================================
# PATCH /api/products/{id}/stock — Negative Case (404 Not Found)
# ============================================================================


@allure.feature("Inventory Service API")
@allure.severity(allure.severity_level.NORMAL)
@allure.description("Attempt to update stock for non-existent product — expect 404")
def test_patch_stock_returns_404_for_unknown_id(base_url: str):
    fake_id = str(uuid.uuid4())
    response = requests.patch(
        f"{base_url}/api/products/{fake_id}/stock",
        json={"delta": -1},
    )

    assert response.status_code == 404
    data = response.json()
    assert "detail" in data
