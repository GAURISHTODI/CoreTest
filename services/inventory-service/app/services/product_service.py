"""
Business logic for product/inventory operations.

Phase 1: basic CRUD + stock delta with negative-stock guard.
Phase 2 (TODO): Redis caching for GET /products, Kafka consumer integration.
"""

import logging
from uuid import UUID

from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from app.models.product import Product

logger = logging.getLogger(__name__)


def list_products(db: Session) -> list[Product]:
    """Return all products with current stock levels."""
    # TODO (Phase 2): Check Redis cache first, fall back to DB
    products = db.query(Product).all()
    logger.debug("Listed %d products", len(products))
    return products


def get_product_by_id(db: Session, product_id: UUID) -> Product:
    """
    Return a single product by ID.
    Raises 404 if not found.
    """
    product = db.query(Product).filter(Product.id == product_id).first()
    if product is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Product not found: {product_id}",
        )
    return product


def update_stock(db: Session, product_id: UUID, delta: int) -> Product:
    """
    Apply a stock delta to a product.

    Used by the Kafka consumer (Phase 2) with delta = -quantity.
    Returns 409 Conflict if the resulting stock would go negative.
    Returns 404 if product not found.
    """
    product = get_product_by_id(db, product_id)

    new_stock = product.stock + delta
    if new_stock < 0:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"Insufficient stock: current={product.stock}, requested delta={delta}",
        )

    product.stock = new_stock
    db.commit()
    db.refresh(product)

    logger.info(
        "Stock updated: product_id=%s, delta=%d, new_stock=%d",
        product_id, delta, new_stock,
    )
    # TODO (Phase 2): Invalidate Redis cache for this product
    return product
