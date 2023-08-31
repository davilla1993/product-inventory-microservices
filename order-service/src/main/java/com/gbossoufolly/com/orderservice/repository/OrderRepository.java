package com.gbossoufolly.com.orderservice.repository;

import com.gbossoufolly.com.orderservice.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
