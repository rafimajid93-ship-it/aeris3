package com.aeris2.repository;

import com.aeris2.model.Order;
import com.aeris2.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user", "items", "items.product", "payment"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.product", "payment"})
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.product", "payment"})
    Page<Order> findAllByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.product", "payment"})
    List<Order> findAllByUser_Id(Long userId);

    @EntityGraph(attributePaths = {"user", "items", "items.product", "payment"})
    Page<Order> findAllByCreatedAtBetween(Instant from, Instant to, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    long countByStatus(OrderStatus orderStatus);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status <> 'CANCELLED'")
    Optional<BigDecimal> sumAllOrderTotals();

    List<Order> findByUser_Id(Long userId);
}
