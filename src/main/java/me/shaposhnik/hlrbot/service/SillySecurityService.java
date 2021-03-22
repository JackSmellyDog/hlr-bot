package me.shaposhnik.hlrbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.persistence.entity.AllowedTelegramId;
import me.shaposhnik.hlrbot.persistence.repository.AllowedTelegramIdRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SillySecurityService {
    private final AllowedTelegramIdRepository allowedTelegramIdRepository;


    public boolean isTelegramIdAllowed(String telegramId) {
        return allowedTelegramIdRepository.findByTelegramId(telegramId).isPresent();
    }

    public void addTelegramId(String telegramId) {
        final String trimmedTelegramId = Objects.requireNonNull(telegramId, "Telegram Id can't be null").trim();

        final AllowedTelegramId allowedTelegramId = new AllowedTelegramId();
        allowedTelegramId.setTelegramId(trimmedTelegramId);

        allowedTelegramIdRepository.save(allowedTelegramId);
    }

}
