package com.coretest.order.dto;

import com.coretest.order.model.Order;
import com.coretest.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response body for order endpoints.
 * Matches API_SPEC.md contract:
 *   { "id": "uuid", "productId": "uuid", "quantity": 2,
 *     "status": "PENDING", "createdAt": "2026-08-22T10:00:00Z" }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private UUID id;
    private UUID productId;
    private int quantity;
    private OrderStatus status;
    private Instant createdAt;

    /**
     * Factory method to convert JPA entity → response DTO.
     */
    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
