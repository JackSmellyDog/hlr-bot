package me.shaposhnik.hlrbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.bot.HlrBotProperties;
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
    private final HlrBotProperties hlrBotProperties;

    public Optional<BotUser> findBotUser(User user) {
        return repository.findById(user.getId());
    }

    public BotUser addUser(User user) {
        BotUser botUser = mapTelegramUserToBotUser(user);

        return repository.save(botUser);
    }

    private BotUser mapTelegramUserToBotUser(User user) {
        final Locale locale = Optional.ofNullable(user.getLanguageCode())
            .filter(languageCode -> hlrBotProperties.getLanguages().contains(languageCode))
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

    public void updateLanguageIfChanged(BotUser botUser, String newLanguageCode) {
        final String currentLanguageCode = botUser.getLocale().getLanguage();
        if (hlrBotProperties.getLanguages().contains(newLanguageCode)
            && !currentLanguageCode.equalsIgnoreCase(newLanguageCode)) {

            botUser.setLocale(Locale.forLanguageTag(newLanguageCode));
            repository.save(botUser);

            log.info("Locale has been changed for user {}, from {} to {}",
                botUser.getUserName(), currentLanguageCode, newLanguageCode);
        }
    }
}
