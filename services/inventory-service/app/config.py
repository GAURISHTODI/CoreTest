"""
Configuration loaded from environment variables.
Matches connection strings from .env.example.
"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """
    Application settings — reads from environment variables,
    falls back to defaults matching .env.example.
    """

    # PostgreSQL (Docker: inventory-db on port 5433)
    inventory_db_url: str = "postgresql+psycopg://postgres:changeme@localhost:5433/inventory_db"

    # Redis (Phase 2)
    redis_host: str = "localhost"
    redis_port: int = 6379

    # Kafka (Phase 2)
    kafka_bootstrap_servers: str = "localhost:9094"
    kafka_topic_order_placed: str = "order.placed"

    class Config:
        env_file = ".env"
        extra = "ignore"


settings = Settings()
