package me.shaposhnik.hlrbot.persistence.repository;

import me.shaposhnik.hlrbot.persistence.entity.AllowedTelegramId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllowedTelegramIdRepository extends JpaRepository<AllowedTelegramId, Long> {
    Optional<AllowedTelegramId> findByTelegramId(String telegramId);
}
