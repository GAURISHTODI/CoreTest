"""
SQLAlchemy model for the products table.

Schema matches ARCHITECTURE.md §6 Data Model:
  id (UUID), name (string), stock (int), price (decimal)

Uses a portable UUID column type that works with both
PostgreSQL (native UUID) and SQLite (CHAR(36)) for local dev.
"""

import uuid

from sqlalchemy import Column, Integer, Numeric, String, TypeDecorator, CHAR
from sqlalchemy.dialects.postgresql import UUID as PG_UUID

from app.database import Base


class PortableUUID(TypeDecorator):
    """Platform-independent UUID type.
    Uses PostgreSQL's native UUID when available, otherwise CHAR(36).
    """
    impl = CHAR(36)
    cache_ok = True

    def load_dialect_impl(self, dialect):
        if dialect.name == "postgresql":
            return dialect.type_descriptor(PG_UUID(as_uuid=True))
        return dialect.type_descriptor(CHAR(36))

    def process_bind_param(self, value, dialect):
        if value is not None:
            return str(value)
        return value

    def process_result_value(self, value, dialect):
        if value is not None:
            return uuid.UUID(str(value))
        return value


class Product(Base):
    """Products table — managed by the Inventory Service."""

    __tablename__ = "products"

    id = Column(PortableUUID(), primary_key=True, default=uuid.uuid4)
    name = Column(String(255), nullable=False)
    stock = Column(Integer, nullable=False, default=0)
    price = Column(Numeric(10, 2), nullable=False)

    def __repr__(self) -> str:
        return f"<Product(id={self.id}, name='{self.name}', stock={self.stock}, price={self.price})>"
