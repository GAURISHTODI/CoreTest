"""
Business logic for product/inventory operations.

Phase 1: basic CRUD + stock delta with negative-stock guard.
Phase 2 (TODO): Redis caching for GET /products, Kafka consumer integration.
"""

import json
import logging
from uuid import UUID

import redis
from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from app.config import settings
from app.models.product import Product
from app.schemas.product import ProductResponse

logger = logging.getLogger(__name__)

# Initialize Redis client
redis_client = redis.Redis(
    host=settings.redis_host,
    port=settings.redis_port,
    decode_responses=True
)

CACHE_KEY_ALL = "products:all"

def _serialize_product(product: Product) -> dict:
    # Use Pydantic schema to convert to dict easily, handling UUID/Decimal
    return ProductResponse.model_validate(product).model_dump(mode='json')

def list_products(db: Session) -> list[Product]:
    """Return all products with current stock levels."""
    try:
        cached = redis_client.get(CACHE_KEY_ALL)
        if cached:
            logger.debug("Cache hit for all products")
            # Return list of dicts. FastAPI response_model handles this.
            return json.loads(cached)
    except Exception as e:
        logger.error(f"Redis error: {e}")

    products = db.query(Product).all()
    logger.debug("Listed %d products from DB", len(products))
    
    try:
        serialized = [_serialize_product(p) for p in products]
        redis_client.setex(CACHE_KEY_ALL, 60, json.dumps(serialized))
    except Exception as e:
        logger.error(f"Redis error setting cache: {e}")

    return products

def get_product_by_id(db: Session, product_id: UUID) -> Product:
    """
    Return a single product by ID.
    Raises 404 if not found.
    """
    cache_key = f"product:{product_id}"
    try:
        cached = redis_client.get(cache_key)
        if cached:
            logger.debug(f"Cache hit for product {product_id}")
            return json.loads(cached)
    except Exception as e:
        logger.error(f"Redis error: {e}")

    product = db.query(Product).filter(Product.id == product_id).first()
    if product is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Product not found: {product_id}",
        )
        
    try:
        redis_client.setex(cache_key, 60, json.dumps(_serialize_product(product)))
    except Exception as e:
        logger.error(f"Redis error setting cache: {e}")

    return product

def update_stock(db: Session, product_id: UUID, delta: int) -> Product:
    """
    Apply a stock delta to a product.

    Used by the Kafka consumer (Phase 2) with delta = -quantity.
    Returns 409 Conflict if the resulting stock would go negative.
    Returns 404 if product not found.
    """
    product = get_product_by_id(db, product_id)

    # Note: If get_product_by_id returned a dict from cache, we need the ORM model to update.
    if isinstance(product, dict):
        product = db.query(Product).filter(Product.id == product_id).first()
        if not product:
            raise HTTPException(status_code=404, detail="Product not found")

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
    
    # Invalidate Redis cache for this product and the all-products list
    try:
        redis_client.delete(f"product:{product_id}", CACHE_KEY_ALL)
    except Exception as e:
        logger.error(f"Redis error invalidating cache: {e}")
        
    return product
