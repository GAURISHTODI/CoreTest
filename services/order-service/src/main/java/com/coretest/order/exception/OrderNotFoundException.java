package com.coretest.order.exception;

import java.util.UUID;

/**
 * Thrown when an order is requested by ID but does not exist.
 * Results in HTTP 404 via GlobalExceptionHandler.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super("Order not found: " + id);
    }
}
