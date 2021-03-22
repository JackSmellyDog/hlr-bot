package me.shaposhnik.hlrbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.service.SillySecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class HlrBot extends AbstractTelegramBot {
    private final SillySecurityService sillySecurityService;
    private final DummyStateMachine dummyStateMachine;

    @Value("${telegram-bot.name}")
    private String botUsername;

    @Value("${telegram-bot.token}")
    private String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            final Message message = update.getMessage();
            final User user = message.getFrom();

            log.info("Oh, hi: {}, with id: {}", user.getUserName(), user.getId());

        } else {
            log.warn("Update has no message. Update: ({})", update);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
