package com.coretest.order.repository;

import com.coretest.order.model.Order;
import com.coretest.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the orders table.
 * Provides built-in CRUD + custom query for status filtering
 * (supports GET /api/orders?status=PENDING).
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatus(OrderStatus status);
}
