"""
Pydantic schemas for the Inventory Service API.

Shapes match API_SPEC.md contracts exactly.
"""

from decimal import Decimal
from uuid import UUID

from pydantic import BaseModel


class ProductResponse(BaseModel):
    """
    Response body for GET /api/products and GET /api/products/{id}.
    """
    id: UUID
    name: str
    stock: int
    price: Decimal

    model_config = {"from_attributes": True}


class StockUpdateRequest(BaseModel):
    """
    Request body for PATCH /api/products/{id}/stock.
    API_SPEC.md: { "delta": -2 }
    """
    delta: int


class StockUpdateResponse(BaseModel):
    """
    Response body for PATCH /api/products/{id}/stock.
    API_SPEC.md: { "id": "uuid", "stock": 8 }
    """
    id: UUID
    stock: int

    model_config = {"from_attributes": True}
