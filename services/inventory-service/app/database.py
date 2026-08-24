"""
SQLAlchemy engine and session setup for the Inventory Service.
"""

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from app.config import settings

engine = create_engine(settings.inventory_db_url, echo=True)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


class Base(DeclarativeBase):
    """Declarative base for all SQLAlchemy models."""
    pass


def get_db():
    """
    FastAPI dependency — yields a DB session per request,
    ensures cleanup on completion.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
