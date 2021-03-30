package me.shaposhnik.hlrbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.bot.enums.Command;
import me.shaposhnik.hlrbot.integration.bsg.BsgAccountService;
import me.shaposhnik.hlrbot.integration.bsg.dto.ApiKey;
import me.shaposhnik.hlrbot.model.AccountBalance;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.HlrId;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.model.enums.UserState;
import me.shaposhnik.hlrbot.persistence.entity.BotUser;
import me.shaposhnik.hlrbot.service.ApiKeyValidationService;
import me.shaposhnik.hlrbot.service.BotUserService;
import me.shaposhnik.hlrbot.service.HlrService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static me.shaposhnik.hlrbot.bot.enums.Command.*;
import static me.shaposhnik.hlrbot.model.enums.UserState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HlrBot extends AbstractTelegramBot {
    private static final List<List<Command>> DEFAULT_KEYBOARD = List.of(List.of(HLR, BALANCE), List.of(MENU));

    private final BotUserService botUserService;
    private final HlrService hlrService;
    private final BsgAccountService accountService;
    private final ApiKeyValidationService apiKeyValidationService;

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
        Command.fromString(message.getText())
            .filter(command -> command == MENU)
            .ifPresentOrElse(command -> handleIncomeCommand(command, botUser), () -> {
                List<HlrId> hlrIds = hlrService.sendHlrs(Phone.fromString(message.getText()), botUser.getApiKey());
                checkHlrStatuses(hlrIds, botUser);

                botUser.setState(ACTIVE);
                botUserService.update(botUser);
            });
    }

    @Async
    protected void checkHlrStatuses(List<HlrId> hlrIds, BotUser botUser) {
        try {
            Thread.sleep(15_000);

            final String message = hlrIds.stream()
                .map(hlrId -> hlrService.getHlrInfo(hlrId, botUser.getApiKey()))
                .map(Hlr::toString)
                .collect(Collectors.joining("\n"));

            sendMessageWithButtons(botUser.getTelegramId(), message, createReplyKeyboardMarkup(DEFAULT_KEYBOARD));

        } catch (Exception e) {
            log.error("Failed to check hlr statues!", e);
        }
    }

    private void handleNewState(Message message, BotUser botUser) {
        if (apiKeyValidationService.isApiKeyValid(ApiKey.of(message.getText()))) {

            botUser.setState(ACTIVE);
            botUser.setApiKey(message.getText());
            botUserService.update(botUser);

            sendMessageWithButtons(botUser.getTelegramId(), "Api Key has been accepted!", createReplyKeyboardMarkup(DEFAULT_KEYBOARD));

        } else {
            sendSimpleMessage(botUser.getTelegramId(), "Give me your token!");
        }
    }

    private void handleActiveState(Message message, BotUser botUser) {
        Command.fromString(message.getText())
            .ifPresentOrElse(command -> handleIncomeCommand(command, botUser), () -> log.info("Message ({}) ignored", message.getText()));
    }

    private void handleIncomeCommand(Command command, BotUser botUser) {
        if (command == HLR) {
            sendMessageWithButtons(botUser.getTelegramId(), "Send me the number you want to hlr!", createReplyKeyboardMarkup(List.of()));

            botUser.setState(SENDING_NUMBERS);
            botUserService.update(botUser);

        } else if (command == MENU) {

            botUser.setState(ACTIVE);
            botUserService.update(botUser);

        } else if (command == START) {
            sendSimpleMessage(botUser.getTelegramId(), "Give me your token!");
        } else if (command == BALANCE) {
            AccountBalance accountBalance = accountService.checkBalance(ApiKey.of(botUser.getApiKey()));

            sendMessageWithButtons(botUser.getTelegramId(), accountBalance.toString(), createReplyKeyboardMarkup(DEFAULT_KEYBOARD));
        } else {
            throw new IllegalStateException("Unhandled command is present");
        }

    }

    private ReplyKeyboardMarkup createReplyKeyboardMarkup(List<List<Command>> keyboard) {

        List<KeyboardRow> keyboardRows = keyboard.stream()
            .filter(not(List::isEmpty))
            .map(this::mapCommandsListToKeyboardRow)
            .collect(Collectors.toList());

        return ReplyKeyboardMarkup.builder()
            .clearKeyboard()
            .keyboard(keyboardRows)
            .resizeKeyboard(true)
            .build();
    }

    @NotNull
    private KeyboardRow mapCommandsListToKeyboardRow(List<Command> commandList) {
        KeyboardRow row = new KeyboardRow();
        commandList.stream()
            .map(Command::asButton)
            .forEach(row::add);

        return row;
    }


}
