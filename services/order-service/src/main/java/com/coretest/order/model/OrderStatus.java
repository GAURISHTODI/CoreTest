package com.coretest.order.model;

/**
 * Order lifecycle statuses per ARCHITECTURE.md data model.
 *
 * PENDING   — order created, awaiting inventory confirmation
 * CONFIRMED — inventory successfully decremented via Kafka consumer
 * FAILED    — inventory decrement failed (e.g., insufficient stock)
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    FAILED
}
