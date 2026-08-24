import asyncio
import json
import logging
from confluent_kafka import Consumer, KafkaError, KafkaException
from app.config import settings
from app.database import SessionLocal
from app.services import product_service

logger = logging.getLogger(__name__)

def consume_events_sync():
    conf = {
        'bootstrap.servers': settings.kafka_bootstrap_servers,
        'group.id': "inventory_service_group",
        'auto.offset.reset': 'earliest',
    }
    
    consumer = Consumer(conf)
    
    # Try to connect/subscribe
    while True:
        try:
            consumer.subscribe([settings.kafka_topic_order_placed])
            logger.info("Connected to Kafka and subscribed to topic")
            break
        except Exception as e:
            logger.error(f"Error subscribing to Kafka, retrying in 5s: {e}")
            import time
            time.sleep(5)

    try:
        while True:
            msg = consumer.poll(timeout=1.0)
            if msg is None:
                continue
            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    continue
                else:
                    logger.error(f"Kafka error: {msg.error()}")
                    continue

            try:
                order_event = json.loads(msg.value().decode('utf-8'))
                logger.info(f"Consumed message: {order_event}")
                
                product_id = order_event.get("productId")
                quantity = order_event.get("quantity")
                
                if not product_id or not quantity:
                    logger.error("Invalid event format: missing productId or quantity")
                    continue
                
                db = SessionLocal()
                try:
                    product_service.update_stock(db, product_id, -quantity)
                    logger.info(f"Successfully decremented stock for product {product_id} by {quantity}")
                except Exception as e:
                    logger.error(f"Failed to update stock for product {product_id}: {e}")
                finally:
                    db.close()
            except Exception as e:
                logger.error(f"Error processing message: {e}")
    finally:
        consumer.close()

def start_kafka_consumer():
    """Starts the Kafka consumer in the background."""
    asyncio.create_task(asyncio.to_thread(consume_events_sync))
