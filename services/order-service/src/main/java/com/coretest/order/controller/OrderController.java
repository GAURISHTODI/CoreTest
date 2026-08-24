package com.coretest.order.controller;

import com.coretest.order.dto.CreateOrderRequest;
import com.coretest.order.dto.OrderResponse;
import com.coretest.order.model.Order;
import com.coretest.order.model.OrderStatus;
import com.coretest.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Order Service.
 *
 * Endpoints per API_SPEC.md:
 *   POST   /api/orders           — create order (201)
 *   GET    /api/orders/{id}      — get single order (200 / 404)
 *   GET    /api/orders?status=   — list orders, optional status filter (200)
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestParam(required = false) OrderStatus status) {
        List<OrderResponse> orders = orderService.listOrders(status).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }
}
