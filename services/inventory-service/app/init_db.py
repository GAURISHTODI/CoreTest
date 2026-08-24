"""
Seed script — populates the products table with sample data for manual testing.

Usage: python -m app.init_db
"""

import logging
from decimal import Decimal
from uuid import uuid4

from app.database import Base, SessionLocal, engine
from app.models.product import Product

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Sample products for manual testing and demos
SEED_PRODUCTS = [
    Product(id=uuid4(), name="Wireless Mouse", stock=50, price=Decimal("29.99")),
    Product(id=uuid4(), name="USB-C Hub", stock=30, price=Decimal("49.99")),
    Product(id=uuid4(), name="Mechanical Keyboard", stock=25, price=Decimal("89.99")),
    Product(id=uuid4(), name="27\" Monitor", stock=15, price=Decimal("349.99")),
    Product(id=uuid4(), name="Laptop Stand", stock=40, price=Decimal("39.99")),
]


def seed_database() -> None:
    """Create tables and insert sample products if the table is empty."""
    logger.info("Creating database tables...")
    Base.metadata.create_all(bind=engine)

    db = SessionLocal()
    try:
        existing_count = db.query(Product).count()
        if existing_count > 0:
            logger.info("Products table already has %d rows — skipping seed.", existing_count)
            return

        for product in SEED_PRODUCTS:
            db.add(product)
        db.commit()
        logger.info("Seeded %d products.", len(SEED_PRODUCTS))
    finally:
        db.close()


if __name__ == "__main__":
    seed_database()
