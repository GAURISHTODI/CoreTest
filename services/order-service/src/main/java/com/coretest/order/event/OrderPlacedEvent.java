package com.coretest.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Kafka event published to the "order.placed" topic.
 *
 * Contract per API_SPEC.md:
 *   { "orderId": "uuid", "productId": "uuid", "quantity": 2,
 *     "timestamp": "2026-08-22T10:00:00Z" }
 *
 * Producer: Order Service, on successful POST /orders
 * Consumer: Inventory Service, triggers stock decrement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacedEvent {

    private UUID orderId;
    private UUID productId;
    private int quantity;
    private Instant timestamp;
}
