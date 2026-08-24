package com.coretest.order.service;

import com.coretest.order.dto.CreateOrderRequest;
import com.coretest.order.exception.OrderNotFoundException;
import com.coretest.order.model.Order;
import com.coretest.order.model.OrderStatus;
import com.coretest.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for order operations.
 *
 * Creates orders as PENDING and publishes order.placed events to Kafka
 * for the Inventory Service to consume and decrement stock.
 */
@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    /**
     * Constructor — eventPublisher is optional (null when Kafka is disabled in test profile).
     */
    public OrderService(OrderRepository orderRepository,
                        @Autowired(required = false) OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a new order with status PENDING, then publish
     * an order.placed Kafka event for inventory processing.
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

        // Publish order.placed event to Kafka (async, non-blocking)
        if (eventPublisher != null) {
            eventPublisher.publish(saved);
        } else {
            log.warn("Kafka publisher not available — skipping order.placed event for orderId={}", saved.getId());
        }

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
