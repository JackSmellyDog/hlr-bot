package me.shaposhnik.hlrbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.bot.enums.Command;
import me.shaposhnik.hlrbot.model.enums.UserState;
import me.shaposhnik.hlrbot.persistence.entity.BotUser;
import me.shaposhnik.hlrbot.service.BotUserService;
import me.shaposhnik.hlrbot.service.HlrService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static me.shaposhnik.hlrbot.bot.enums.Command.*;
import static me.shaposhnik.hlrbot.model.enums.UserState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HlrBot extends AbstractTelegramBot {

    private final BotUserService botUserService;
    private final HlrService hlrService;

    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            final Message message = update.getMessage();
            handleIncomeMessage(message);

        } else {
            log.warn("Update has no message. Update: ({})", update);
        }
    }

    private void handleIncomeMessage(Message message) {
        final User user = message.getFrom();
        log.info("Oh, hi: {}, with id: {}", user.getUserName(), user.getId());

        sendSimpleMessage(user.getId(), "\uD83D\uDCB0");

        final BotUser botUser = botUserService.findBotUser(user).orElseGet(() -> botUserService.addUser(user));
        final UserState state = botUser.getState();

        if (state == ACTIVE) {
            handleActiveState(message, botUser);

        } else if (state == NEW) {
            handleNewState(message, botUser);

        } else if (state == SENDING_NUMBERS) {
            handleSendingNumbersState(message, botUser);

        } else {
            throw new IllegalStateException("Unhandled state is present");
        }

    }

    private void handleSendingNumbersState(Message message, BotUser botUser) {
        if (message.isCommand()) {
            sendSimpleMessage(botUser.getTelegramId(), "!");
        }
    }

    private void handleNewState(Message message, BotUser botUser) {
        if (message.isCommand()) {
            sendSimpleMessage(botUser.getTelegramId(), "Give me your token!");
        }
    }

    private void handleActiveState(Message message, BotUser botUser) {
        if (message.isCommand()) {
            log.info("This is command!: {}", message.getText());

            Command.fromString(message.getText())
                .ifPresent(command -> handleIncomeCommand(command, botUser));
        }
    }

    private void handleIncomeCommand(Command command, BotUser botUser) {
        if (command == HLR) {
            sendSimpleMessage(botUser.getTelegramId(), "Send me the number you want to hlr!");

            botUser.setState(SENDING_NUMBERS);
            botUserService.update(botUser);

        } else if (command == STOP) {

            botUser.setState(ACTIVE);
            botUserService.update(botUser);

        } else if (command == START) {
            sendSimpleMessage(botUser.getTelegramId(), "Give me your token!");
        } else if (command == STATUS) {
            sendSimpleMessage(botUser.getTelegramId(), "Status command!");
        } else {
            throw new IllegalStateException("Unhandled command is present");
        }

    }


}
