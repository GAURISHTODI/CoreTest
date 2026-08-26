"""
Pytest fixtures for the Inventory Service API tests.

Provides:
  - base_url: configurable base URL for the Inventory Service
  - existing_product: a dict with a real product fetched from the running service
  - existing_product_id: just the UUID string of that product
"""

import os

import pytest
import requests


@pytest.fixture(scope="session")
def base_url() -> str:
    """Base URL for the Inventory Service, configurable via env var."""
    return os.getenv("INVENTORY_BASE_URL", "http://localhost:8000")


@pytest.fixture(scope="session")
def existing_product(base_url: str) -> dict:
    """
    Fetch the first product from the Inventory Service seed data.
    Fails fast if no products are available.
    """
    response = requests.get(f"{base_url}/api/products")
    assert response.status_code == 200, (
        f"Failed to fetch products from {base_url}/api/products: {response.status_code}"
    )
    products = response.json()
    assert len(products) > 0, "No products found — ensure seed data is loaded"
    return products[0]


@pytest.fixture(scope="session")
def existing_product_id(existing_product: dict) -> str:
    """UUID string of a known-good product for test reuse."""
    return existing_product["id"]
