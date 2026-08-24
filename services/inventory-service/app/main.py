"""
FastAPI application entry point for the Inventory Service.

Run with: uvicorn app.main:app --port 8000 --reload
"""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.database import Base, engine
from app.routers import products
from app.kafka_consumer import start_kafka_consumer

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Startup: create DB tables and start Kafka consumer.
    Shutdown: cleanup.
    """
    logger.info("Creating database tables...")
    Base.metadata.create_all(bind=engine)
    logger.info("Database tables ready.")
    
    # Start Kafka consumer
    start_kafka_consumer()
    logger.info("Kafka consumer started.")
    
    yield
    logger.info("Inventory Service shutting down.")


app = FastAPI(
    title="Inventory Service",
    description="Product catalog and stock management — CoreTest project",
    version="0.1.0",
    lifespan=lifespan,
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routers
app.include_router(products.router)


@app.get("/health", tags=["health"])
def health_check() -> dict:
    """Basic health check endpoint."""
    return {"status": "healthy", "service": "inventory-service"}
