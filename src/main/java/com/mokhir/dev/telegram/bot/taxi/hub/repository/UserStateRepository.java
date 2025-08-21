package com.mokhir.dev.telegram.bot.taxi.hub.repository;

import com.mokhir.dev.telegram.bot.taxi.hub.entity.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStateRepository extends JpaRepository<UserState, Long> {
    Optional<UserState> findByUserId(Long userId);
}
