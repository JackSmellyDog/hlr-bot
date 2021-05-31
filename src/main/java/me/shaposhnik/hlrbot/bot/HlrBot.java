package me.shaposhnik.hlrbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.bot.enums.Command;
import me.shaposhnik.hlrbot.converter.HlrToTelegramResponseConverter;
import me.shaposhnik.hlrbot.exception.BaseException;
import me.shaposhnik.hlrbot.integration.bsg.BsgAccountService;
import me.shaposhnik.hlrbot.integration.bsg.dto.ApiKey;
import me.shaposhnik.hlrbot.model.*;
import me.shaposhnik.hlrbot.model.enums.UserState;
import me.shaposhnik.hlrbot.persistence.entity.BotUser;
import me.shaposhnik.hlrbot.service.BotUserService;
import me.shaposhnik.hlrbot.service.HlrAsyncService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static me.shaposhnik.hlrbot.bot.enums.Command.*;
import static me.shaposhnik.hlrbot.model.enums.UserState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HlrBot extends AbstractTelegramBot {
    private static final List<List<Command>> DEFAULT_KEYBOARD = List.of(
        List.of(HLR, ID),
        List.of(BALANCE, CHANGE_API_KEY),
        List.of(DISCARD_STATE)
    );

    private static final String TOKEN_REQUIRED_MESSAGE = "Give me your token!";
    private static final String TOKEN_INVALID_MESSAGE = "It's an invalid token. Please send again!";
    private static final String TOKEN_ACCEPTED_MESSAGE = "Api Key has been accepted!";
    private static final String NUMBER_FOR_HLR_REQUIRED_TEMPLATE = "Send me up to %s numbers you want to hlr!";
    private static final String ID_FOR_HLR_REQUIRED = "Send me the ID of previous HLR request!";

    private static final String TOO_MANY_PHONES_MESSAGE_TEMPLATE =
        "Phone(s): %s was/were ignored. Please, send them in the next request.";

    private final BotUserService botUserService;
    private final HlrAsyncService hlrService;
    private final BsgAccountService accountService;
    private final HlrToTelegramResponseConverter hlrToTelegramResponseConverter;

    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.limit-of-numbers}")
    private int limitOfNumbers;

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
            log.info("Update has no message. Update: ({})", update);
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

        } else if (state == SENDING_ID) {
            handleSendingIdState(message, botUser);

        } else if (state == SENDING_API_KEY) {
            handleSendingApiKeyState(message, botUser);

        } else {
            throw new IllegalStateException("Unhandled state is present");
        }

    }

    // TODO: 5/16/21 Refactor this crap
    private void handleSendingApiKeyState(Message message, BotUser botUser) {
        Command.fromString(message.getText())
            .filter(command -> command == DISCARD_STATE)
            .ifPresentOrElse(
                command -> handleIncomeCommand(command, botUser),
                () -> acceptNewToken(message, botUser, TOKEN_INVALID_MESSAGE)
            );
    }

    private void acceptNewToken(Message message, BotUser botUser, String warningMessageText) {
        if (accountService.isApiKeyValid(ApiKey.of(message.getText()))) {

            botUser.setState(ACTIVE);
            botUser.setApiKey(message.getText());
            botUserService.update(botUser);

            sendMessageWithButtons(botUser.getId(), TOKEN_ACCEPTED_MESSAGE, createReplyKeyboardMarkup(DEFAULT_KEYBOARD));

        } else {
            sendSimpleMessage(botUser.getId(), warningMessageText);
        }
    }

    // TODO: 4/15/21 Refactor this crap
    private void handleSendingIdState(Message message, BotUser botUser) {
        Command.fromString(message.getText())
            .filter(command -> command == DISCARD_STATE)
            .ifPresentOrElse(command -> handleIncomeCommand(command, botUser), () -> {

                final ReplyKeyboardMarkup replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);
                try {
                    final Hlr hlr = hlrService.getHlrInfo(HlrId.of(message.getText()), botUser.getApiKey());
                    final String response = hlrToTelegramResponseConverter.convert(hlr);
                    sendMessageWithButtons(botUser.getId(), response, replyKeyboardMarkup);

                } catch (BaseException e) {
                    sendMessageWithButtons(botUser.getId(), e.getMessage(), replyKeyboardMarkup);
                }

                botUser.setState(ACTIVE);
                botUserService.update(botUser);
            });
    }

    // TODO: 4/15/21 Refactor this crap
    private void handleSendingNumbersState(Message message, BotUser botUser) {
        Command.fromString(message.getText())
            .filter(command -> command == DISCARD_STATE)
            .ifPresentOrElse(command -> handleIncomeCommand(command, botUser), () -> {
                var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

                try {
                    List<Phone> receivedPhones = Phone.fromString(message.getText());

                    List<Phone> phones = limitPhones(receivedPhones);
                    List<Phone> ignoredPhones = getIgnoredPhones(receivedPhones);

                    if (!ignoredPhones.isEmpty()) {
                        final String tooManyPhonesMessage = String.format(TOO_MANY_PHONES_MESSAGE_TEMPLATE, ignoredPhones);
                        sendMessageWithButtons(botUser.getId(), tooManyPhonesMessage, replyKeyboardMarkup);
                    }

                    List<HlrIdPhonePair> hlrIdPhonePairs = hlrService.sendHlrs(phones, botUser.getApiKey());

                    hlrService.getHlrInfoListAsync(hlrIdPhonePairs, botUser.getApiKey())
                        .whenComplete((result, error) -> whenHlrInfoComplete(botUser.getId(), result, error));

                } catch (BaseException e) {
                    sendMessageWithButtons(botUser.getId(), e.getMessage(), replyKeyboardMarkup);
                }

                botUser.setState(ACTIVE);
                botUserService.update(botUser);
            });
    }

    private List<Phone> limitPhones(List<Phone> phones) {
        if (phones.size() > limitOfNumbers) {
            return List.copyOf(phones.subList(0, limitOfNumbers));
        }

        return phones;
    }

    private List<Phone> getIgnoredPhones(List<Phone> phones) {
        if (phones.size() > limitOfNumbers) {
            return List.copyOf(phones.subList(limitOfNumbers, phones.size()));
        }

        return List.of();
    }

    private void whenHlrInfoComplete(Long telegramId, List<Hlr> result, Throwable error) {
        final var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

        if (result != null) {
            result.stream()
                .map(hlrToTelegramResponseConverter::convert)
                .forEach(response -> sendMessageWithButtons(telegramId, response, replyKeyboardMarkup));

        } else if (error instanceof BaseException) {
            sendMessageWithButtons(telegramId, error.getMessage(), replyKeyboardMarkup);

        } else if (error instanceof CompletionException && error.getCause() instanceof BaseException) {
            sendMessageWithButtons(telegramId, error.getCause().getMessage(), replyKeyboardMarkup);

        } else {
            log.error("Error!", error);
        }
    }

    private void handleNewState(Message message, BotUser botUser) {
        acceptNewToken(message, botUser, TOKEN_REQUIRED_MESSAGE);
    }

    private void handleActiveState(Message message, BotUser botUser) {
        Command.fromString(message.getText()).ifPresentOrElse(
            command -> handleIncomeCommand(command, botUser),
            () -> log.info("Message ({}) ignored", message.getText())
        );
    }

    private void handleIncomeCommand(Command command, BotUser botUser) {
        if (command == HLR) {
            final String numberForHlrMessage = String.format(NUMBER_FOR_HLR_REQUIRED_TEMPLATE, limitOfNumbers);
            sendMessageWithButtons(botUser.getId(), numberForHlrMessage, createReplyKeyboardMarkup(List.of()));

            botUser.setState(SENDING_NUMBERS);
            botUserService.update(botUser);

        } else if (command == ID) {

            sendMessageWithButtons(botUser.getId(), ID_FOR_HLR_REQUIRED, createReplyKeyboardMarkup(List.of()));

            botUser.setState(SENDING_ID);
            botUserService.update(botUser);

        } else if (command == DISCARD_STATE) {

            botUser.setState(ACTIVE);
            botUserService.update(botUser);

        } else if (command == CHANGE_API_KEY) {
            sendMessageWithButtons(botUser.getId(), TOKEN_REQUIRED_MESSAGE, createReplyKeyboardMarkup(DEFAULT_KEYBOARD));

            botUser.setState(SENDING_API_KEY);
            botUserService.update(botUser);

        } else if (command == START) {
            sendSimpleMessage(botUser.getId(), TOKEN_REQUIRED_MESSAGE);
        } else if (command == BALANCE) {
            Balance balance = accountService.checkBalance(ApiKey.of(botUser.getApiKey()));

            sendMessageWithButtons(botUser.getId(), balance.toString(), createReplyKeyboardMarkup(DEFAULT_KEYBOARD));
        } else {
            throw new IllegalStateException("Unhandled command is present");
        }

    }

    private ReplyKeyboardMarkup createReplyKeyboardMarkup(List<List<Command>> keyboard) {
        var keyboardRows = keyboard.stream()
            .filter(not(List::isEmpty))
            .map(this::mapCommandsListToKeyboardRow)
            .collect(Collectors.toList());

        return ReplyKeyboardMarkup.builder()
            .clearKeyboard()
            .keyboard(keyboardRows)
            .resizeKeyboard(true)
            .build();
    }

    private KeyboardRow mapCommandsListToKeyboardRow(List<Command> commandList) {
        var row = new KeyboardRow();
        commandList.stream().map(Command::asButton).forEach(row::add);

        return row;
    }

}
