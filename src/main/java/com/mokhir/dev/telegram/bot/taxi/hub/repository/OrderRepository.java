package com.mokhir.dev.telegram.bot.taxi.hub.repository;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
}
