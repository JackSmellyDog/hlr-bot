package me.shaposhnik.hlrbot.persistence.repository;

import me.shaposhnik.hlrbot.persistence.entity.HlrEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HlrEntityRepository extends JpaRepository<HlrEntity, Long> {
    Optional<HlrEntity> findByProviderId(String providerId);
}
