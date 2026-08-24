package com.coretest.order.service;

import com.coretest.order.dto.CreateOrderRequest;
import com.coretest.order.exception.OrderNotFoundException;
import com.coretest.order.model.Order;
import com.coretest.order.model.OrderStatus;
import com.coretest.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for order operations.
 *
 * Phase 1: basic CRUD — create order as PENDING, retrieve by ID, list with filter.
 * Phase 2 (TODO): publish order.placed Kafka event on creation,
 *                  validate productId against Inventory Service before saving.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Create a new order with status PENDING.
     *
     * TODO (Phase 2): Before saving, validate productId exists by calling
     *   GET http://inventory-service:8000/api/products/{productId}
     *   → return 404 if product not found.
     *
     * TODO (Phase 2): After saving, publish order.placed event to Kafka.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        Order order = Order.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(OrderStatus.PENDING)
                .build();

        Order saved = orderRepository.save(order);
        log.info("Order created: id={}, status={}", saved.getId(), saved.getStatus());

        return saved;
    }

    /**
     * Retrieve a single order by ID.
     * @throws OrderNotFoundException if the order doesn't exist (→ 404)
     */
    @Transactional(readOnly = true)
    public Order getOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    /**
     * List all orders, optionally filtered by status.
     * Supports: GET /api/orders?status=PENDING
     */
    @Transactional(readOnly = true)
    public List<Order> listOrders(OrderStatus status) {
        if (status != null) {
            log.debug("Listing orders with status={}", status);
            return orderRepository.findByStatus(status);
        }
        log.debug("Listing all orders");
        return orderRepository.findAll();
    }
}
