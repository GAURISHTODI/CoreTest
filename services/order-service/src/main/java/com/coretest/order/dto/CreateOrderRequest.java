package com.coretest.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request body for POST /api/orders.
 * Matches API_SPEC.md contract:
 *   { "productId": "uuid", "quantity": 2 }
 *
 * Validation:
 *   - productId must not be null (400 if missing)
 *   - quantity must be ≥ 1 (400 if ≤ 0)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "productId is required")
    private UUID productId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;
}
