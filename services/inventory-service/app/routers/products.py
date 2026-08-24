"""
REST endpoints for the Inventory Service.

Endpoints per API_SPEC.md:
  GET    /api/products           — list all products with stock
  GET    /api/products/{id}      — get single product (404 if not found)
  PATCH  /api/products/{id}/stock — update stock via delta (409 if negative)
"""

from uuid import UUID

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas.product import ProductResponse, StockUpdateRequest, StockUpdateResponse
from app.services import product_service

router = APIRouter(prefix="/api/products", tags=["products"])


@router.get("", response_model=list[ProductResponse])
def list_products(db: Session = Depends(get_db)) -> list[ProductResponse]:
    """List all products with current stock levels."""
    products = product_service.list_products(db)
    return [ProductResponse.model_validate(p) for p in products]


@router.get("/{product_id}", response_model=ProductResponse)
def get_product(product_id: UUID, db: Session = Depends(get_db)) -> ProductResponse:
    """Get a single product by ID. Returns 404 if not found."""
    product = product_service.get_product_by_id(db, product_id)
    return ProductResponse.model_validate(product)


@router.patch("/{product_id}/stock", response_model=StockUpdateResponse)
def update_stock(
    product_id: UUID,
    request: StockUpdateRequest,
    db: Session = Depends(get_db),
) -> StockUpdateResponse:
    """
    Update product stock by applying a delta.
    Internal use only — called by the Kafka consumer (Phase 2).
    Returns 409 if resulting stock would be negative.
    """
    product = product_service.update_stock(db, product_id, request.delta)
    return StockUpdateResponse.model_validate(product)
