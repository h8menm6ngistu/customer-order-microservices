package com.example.service.order.repository;

import com.example.service.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    Optional<Order> findOptionalByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}