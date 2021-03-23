package me.shaposhnik.hlrbot.persistence.repository;

import me.shaposhnik.hlrbot.persistence.entity.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotUserRepository extends JpaRepository<BotUser, Long> {
}
