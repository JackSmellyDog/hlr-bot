package me.shaposhnik.hlrbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.model.enums.UserState;
import me.shaposhnik.hlrbot.persistence.entity.BotUser;
import me.shaposhnik.hlrbot.persistence.repository.BotUserRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotUserService {

    private static final Locale DEFAULT_LOCALE = Locale.US;
    private final BotUserRepository repository;

    public Optional<BotUser> findBotUser(User user) {
        return repository.findById(user.getId());
    }

    public BotUser addUser(User user) {
        BotUser botUser = mapTelegramUserToBotUser(user);

        return repository.save(botUser);
    }

    private BotUser mapTelegramUserToBotUser(User user) {
        final Locale locale = Optional.ofNullable(user.getLanguageCode())
            .map(Locale::forLanguageTag)
            .orElse(DEFAULT_LOCALE);

        return BotUser.builder()
            .id(user.getId())
            .userName(user.getUserName())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .locale(locale)
            .state(UserState.NEW)
            .build();
    }

    public void update(BotUser botUser) {
        repository.save(botUser);
    }
}
