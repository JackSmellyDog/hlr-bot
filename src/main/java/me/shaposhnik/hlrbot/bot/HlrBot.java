package me.shaposhnik.hlrbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.bot.enums.Command;
import me.shaposhnik.hlrbot.converter.HlrToTelegramResponseConverter;
import me.shaposhnik.hlrbot.exception.BaseException;
import me.shaposhnik.hlrbot.files.FileService;
import me.shaposhnik.hlrbot.files.readers.PhoneFileReaderFacade;
import me.shaposhnik.hlrbot.files.writers.HlrResultFileWriterFacade;
import me.shaposhnik.hlrbot.integration.bsg.BsgAccountService;
import me.shaposhnik.hlrbot.integration.bsg.dto.ApiKey;
import me.shaposhnik.hlrbot.model.*;
import me.shaposhnik.hlrbot.model.enums.UserState;
import me.shaposhnik.hlrbot.persistence.entity.BotUser;
import me.shaposhnik.hlrbot.service.BotUserService;
import me.shaposhnik.hlrbot.service.HlrAsyncService;
import me.shaposhnik.hlrbot.service.PhoneService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static me.shaposhnik.hlrbot.bot.enums.Command.*;
import static me.shaposhnik.hlrbot.model.enums.UserState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HlrBot extends AbstractTelegramBot {
    private static final List<List<Command>> DEFAULT_KEYBOARD = List.of(
        List.of(HLR, ID, FILE),
        List.of(BALANCE, CHANGE_API_KEY),
        List.of(DISCARD_STATE)
    );

    // TODO: 9/19/21 messages to a separate file
    private static final String TOKEN_REQUIRED_MESSAGE = "Give me your token!";
    private static final String TOKEN_INVALID_MESSAGE = "It's an invalid token. Please send again!";
    private static final String TOKEN_ACCEPTED_MESSAGE = "Api Key has been accepted!";
    private static final String NUMBER_FOR_HLR_REQUIRED_TEMPLATE = "Send me up to %s numbers you want to hlr!";
    private static final String ID_FOR_HLR_REQUIRED = "Send me the ID of previous HLR request!";

    private static final String TOO_MANY_PHONES_MESSAGE_TEMPLATE =
        "Phone(s): %s was/were ignored. Please, send them in the next request.";

    private static final String FILE_MESSAGE = "Send me a file with phone numbers!";
    private static final String NO_FILES_IN_MESSAGE = "Your message has no files attached!";
    private static final String TOO_LARGE_FILE_TEMPLATE = "The file is too large! Files which weight more than %s MB are not allowed!";

    private final BotUserService botUserService;
    private final HlrAsyncService hlrService;
    private final BsgAccountService accountService;
    private final HlrToTelegramResponseConverter hlrToTelegramResponseConverter;
    private final FileService fileService;
    private final PhoneService phoneService;
    private final PhoneFileReaderFacade phoneFileReaderFacade;
    private final HlrResultFileWriterFacade hlrResultFileWriterFacade;

    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.limit-of-numbers}")
    private int limitOfNumbers;

    @Value("#{${bot.files.max-size} * 1024}")
    private int maxFileSizeInBytes;

    @Value("${bot.files.max-size}")
    private int maxFileSizeInMegabytes;

    @Value("${bot.files.directory-to-download}")
    private String fileDownloadDirectory;

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

        } else if (state == SENDING_FILE) {
            handleSendingFileState(message, botUser);

        } else {
            throw new IllegalStateException("Unhandled state is present");
        }
    }

    private void handleSendingFileState(Message message, BotUser botUser) {
        if (notDiscardStateCommand(message)) {
            final var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

            if (!message.hasDocument()) {
                sendMessageWithButtons(botUser.getId(), NO_FILES_IN_MESSAGE, replyKeyboardMarkup);
                return;
            }

            final var document = message.getDocument();

            if (document.getFileSize() > maxFileSizeInBytes) {
                final String errorMessage = String.format(TOO_LARGE_FILE_TEMPLATE, maxFileSizeInMegabytes);
                sendMessageWithButtons(botUser.getId(), errorMessage, replyKeyboardMarkup);
                return;
            }

            try {
                final List<Phone> phones = readPhones(document);

                List<SentHlr> sentHlrList = hlrService.sendHlrs(phones, botUser.getApiKey());

                hlrService.getHlrInfoListAsync(sentHlrList, botUser.getApiKey()).whenComplete((result, error) ->
                        whenHlrInfoCompleteSendAnswerAsFile(botUser.getId(), document.getFileName(), result, error));

            } catch (BaseException e) {
                log.error("!", e);
                sendMessageWithButtons(botUser.getId(), e.getMessage(), replyKeyboardMarkup);
            }

            botUser.setState(ACTIVE);
            botUserService.update(botUser);

        } else {
            handleIncomeCommand(DISCARD_STATE, botUser);
        }
    }

    private List<Phone> readPhones(Document document) {
        Path downloaded = null;

        try {
            downloaded = downloadFile(
                document.getFileId(),
                Files.createTempFile(Path.of(fileDownloadDirectory), document.getFileUniqueId(), document.getFileName())
            );

            return phoneFileReaderFacade.readPhones(downloaded);

        } catch (IOException e) {
            log.error("!", e);
            throw new RuntimeException(e);
        } finally {
            Optional.ofNullable(downloaded).ifPresent(fileService::deleteFile);
        }
    }

    private void sendResultFile(Long telegramId, String fileName, List<Hlr> hlrList) {
        Path responseFile = null;

        // TODO: 9/28/21 figure out something better
        fileName = fileName + ".csv";

        try {
            String prefix = UUID.randomUUID().toString();
            responseFile = hlrResultFileWriterFacade
                .write(Files.createTempFile(Path.of(fileDownloadDirectory), prefix, fileName), hlrList);

            sendFile(String.valueOf(telegramId), responseFile);

        } catch (IOException e) {
            log.error("!", e);
            throw new RuntimeException(e);
        } finally {
            Optional.ofNullable(responseFile).ifPresent(fileService::deleteFile);
        }
    }

    private void handleSendingApiKeyState(Message message, BotUser botUser) {
        if (notDiscardStateCommand(message)) {
            acceptNewToken(message, botUser, TOKEN_INVALID_MESSAGE);
        } else {
            handleIncomeCommand(DISCARD_STATE, botUser);
        }
    }

    private boolean notDiscardStateCommand(Message message) {
        return Command.fromString(message.getText())
            .filter(command -> command == DISCARD_STATE)
            .isEmpty();
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

    private void handleSendingIdState(Message message, BotUser botUser) {
        if (notDiscardStateCommand(message)) {
            final var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);
            try {
                final Hlr hlr = hlrService.getHlrInfoByProviderId(message.getText(), botUser.getApiKey());
                final String response = hlrToTelegramResponseConverter.convert(hlr);
                sendMessageWithButtons(botUser.getId(), response, replyKeyboardMarkup);

            } catch (BaseException e) {
                sendMessageWithButtons(botUser.getId(), e.getMessage(), replyKeyboardMarkup);
            }

            botUser.setState(ACTIVE);
            botUserService.update(botUser);
        } else {
            handleIncomeCommand(DISCARD_STATE, botUser);
        }
    }

    private void handleSendingNumbersState(Message message, BotUser botUser) {
        if (notDiscardStateCommand(message)) {
            var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

            try {
                List<Phone> receivedPhones = phoneService.parseFromString(message.getText());

                List<Phone> phones = phoneService.limitPhones(receivedPhones);
                List<Phone> ignoredPhones = phoneService.getIgnoredPhones(receivedPhones);

                if (!ignoredPhones.isEmpty()) {
                    final String tooManyPhonesMessage = String.format(TOO_MANY_PHONES_MESSAGE_TEMPLATE, ignoredPhones);
                    sendMessageWithButtons(botUser.getId(), tooManyPhonesMessage, replyKeyboardMarkup);
                }

                List<SentHlr> sentHlrList = hlrService.sendHlrs(phones, botUser.getApiKey());

                hlrService.getHlrInfoListAsync(sentHlrList, botUser.getApiKey())
                    .whenComplete((result, error) -> whenHlrInfoComplete(botUser.getId(), result, error));

            } catch (BaseException e) {
                sendMessageWithButtons(botUser.getId(), e.getMessage(), replyKeyboardMarkup);
            }

            botUser.setState(ACTIVE);
            botUserService.update(botUser);
        } else {
            handleIncomeCommand(DISCARD_STATE, botUser);
        }
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
            log.error("Unexpected error:", error);
        }
    }

    private void whenHlrInfoCompleteSendAnswerAsFile(Long telegramId, String fileName, List<Hlr> result, Throwable error) {
        final var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

        if (result != null) {
            sendResultFile(telegramId, fileName, result);
        } else if (error instanceof BaseException) {
            sendMessageWithButtons(telegramId, error.getMessage(), replyKeyboardMarkup);

        } else if (error instanceof CompletionException && error.getCause() instanceof BaseException) {
            sendMessageWithButtons(telegramId, error.getCause().getMessage(), replyKeyboardMarkup);

        } else {
            log.error("Unexpected error:", error);
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

        } else if (command == FILE) {
            sendMessageWithButtons(botUser.getId(), FILE_MESSAGE, createReplyKeyboardMarkup(DEFAULT_KEYBOARD));

            botUser.setState(SENDING_FILE);
            botUserService.update(botUser);

        } else if (command == BALANCE) {
            Balance balance = accountService.checkBalance(ApiKey.of(botUser.getApiKey()));

            sendMessageWithButtons(botUser.getId(), balance.toString(), createReplyKeyboardMarkup(DEFAULT_KEYBOARD));
        } else {
            throw new IllegalStateException("Unhandled command is present");
        }

    }

}
