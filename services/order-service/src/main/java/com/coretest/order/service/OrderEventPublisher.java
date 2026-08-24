package com.coretest.order.service;

import com.coretest.order.event.OrderPlacedEvent;
import com.coretest.order.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Publishes order.placed events to Kafka after successful order creation.
 *
 * Per API_SPEC.md:
 *   Topic: order.placed
 *   Producer: Order Service, on successful POST /orders
 *   Consumer: Inventory Service, triggers PATCH /products/{id}/stock with delta = -quantity
 *
 * This bean is only created when KafkaTemplate is available (i.e., not in test profile).
 */
@Service
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.order-placed:order.placed}")
    private String orderPlacedTopic;

    public OrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish an order.placed event for the given order.
     * Serializes to JSON and sends to Kafka asynchronously.
     */
    public void publish(Order order) {
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(order.getId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .timestamp(Instant.now())
                .build();

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(orderPlacedTopic, order.getId().toString(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish order.placed event: orderId={}", order.getId(), ex);
                        } else {
                            log.info("Published order.placed event: orderId={}, topic={}, offset={}",
                                    order.getId(), orderPlacedTopic,
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order.placed event: orderId={}", order.getId(), e);
        }
    }
}
