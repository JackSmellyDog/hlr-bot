package me.shaposhnik.hlrbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.bot.enums.Command;
import me.shaposhnik.hlrbot.converter.HlrToTelegramResponseConverter;
import me.shaposhnik.hlrbot.exception.BaseException;
import me.shaposhnik.hlrbot.files.enrichers.HlrResultFileEnricherFacade;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.files.readers.PhoneFileReaderFacade;
import me.shaposhnik.hlrbot.files.storage.FileStorage;
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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

    private final BotUserService botUserService;
    private final HlrAsyncService hlrService;
    private final BsgAccountService accountService;
    private final HlrToTelegramResponseConverter hlrToTelegramResponseConverter;
    private final FileStorage fileStorage;
    private final PhoneService phoneService;
    private final PhoneFileReaderFacade phoneFileReaderFacade;
    private final HlrResultFileWriterFacade hlrResultFileWriterFacade;
    private final HlrResultFileEnricherFacade hlrResultFileEnricherFacade;
    private final MessageSource messageSource;

    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.limit-of-numbers}")
    private int limitOfNumbers;

    @Value("${bot.limit-of-numbers-in-file}")
    private int limitOfNumbersInFile;

    @Value("#{${bot.files.max-size} * 1024 * 1024}")
    private int maxFileSizeInBytes;

    @Value("${bot.files.max-size}")
    private int maxFileSizeInMegabytes;

    @Value("${bot.expected-execution-time-per-request-in-milliseconds}")
    private int expectedExecutionTimePerRequest;

    @Value("${bot.long-execution-message-value-in-minutes}")
    private int longExecutionMessageValue;


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

        if (isDiscardStateCommand(message)) {
            handleIncomeCommand(DISCARD_STATE, botUser);
            return;
        }

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

    // TODO: 10/9/21 not allow if user has file in processing
    private void handleSendingFileState(Message message, BotUser botUser) {
        final var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

        if (!message.hasDocument()) {
            final String noFileMessage = getMessage("file.not-attached", botUser.getLocale());
            sendMessageWithButtons(botUser.getId(), noFileMessage, replyKeyboardMarkup);
            return;
        }

        final var document = message.getDocument();

        if (document.getFileSize() > maxFileSizeInBytes) {
            final String errorMessage = getMessage("file.templates.too-large", botUser.getLocale(), maxFileSizeInMegabytes);
            sendMessageWithButtons(botUser.getId(), errorMessage, replyKeyboardMarkup);
            return;
        }

        try {
            FileEntity downloadedFile = fileStorage.save(document, downloadFileAsInputStream(document.getFileId()));
            final List<Phone> phones = phoneFileReaderFacade.readPhones(downloadedFile);

            if (phones.size() > limitOfNumbersInFile) {
                final String tooManyNumbersMessage = getMessage("file.templates.too-many-numbers", botUser.getLocale(), phones.size(), limitOfNumbersInFile);
                sendMessageWithButtons(botUser.getId(), tooManyNumbersMessage, replyKeyboardMarkup);
                return;
            }

            long toMinutes = TimeUnit.MILLISECONDS.toMinutes(expectedExecutionTimePerRequest);
            if (toMinutes >= longExecutionMessageValue) {
                final String tooManyNumbersMessage = getMessage("file.templates.processing-time", botUser.getLocale(), toMinutes);
                sendMessageWithButtons(botUser.getId(), tooManyNumbersMessage, replyKeyboardMarkup);
            }

            List<SentHlr> sentHlrList = hlrService.sendHlrs(phones, botUser.getApiKey());

            hlrService.getHlrInfoListAsync(sentHlrList, botUser.getApiKey()).whenComplete((result, error) -> {
                if (result != null) {
                    whenHlrInfoCompleteSendAnswerAsFile(botUser, downloadedFile, result);
                } else {
                    handleError(error, botUser);
                }
            });

        } catch (BaseException e) {
            log.error("!", e);
            sendMessageWithButtons(botUser.getId(), e.getMessage(), replyKeyboardMarkup);
        } catch (Exception e) {
            log.error("Unexpected error:", e);
            sendMessageWithButtons(botUser.getId(), getMessage("error.default", botUser.getLocale()), replyKeyboardMarkup);
        }

        botUser.setState(ACTIVE);
        botUserService.update(botUser);
    }

    private void handleError(Throwable error, BotUser botUser) {
        final var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

        if (error instanceof BaseException) {
            sendMessageWithButtons(botUser.getId(), error.getMessage(), replyKeyboardMarkup);

        } else if (error instanceof CompletionException && error.getCause() instanceof BaseException) {
            sendMessageWithButtons(botUser.getId(), error.getCause().getMessage(), replyKeyboardMarkup);

        } else {
            log.error("Unexpected error:", error);
            sendMessageWithButtons(botUser.getId(), getMessage("error.default", botUser.getLocale()), replyKeyboardMarkup);
        }
    }

    private void handleSendingApiKeyState(Message message, BotUser botUser) {
        acceptNewToken(message, botUser, getMessage("token.invalid", botUser.getLocale()));
    }

    private boolean isDiscardStateCommand(Message message) {
        return Command.fromString(message.getText())
            .filter(command -> command == DISCARD_STATE)
            .isPresent();
    }

    private void acceptNewToken(Message message, BotUser botUser, String warningMessageText) {
        if (accountService.isApiKeyValid(ApiKey.of(message.getText()))) {

            botUser.setState(ACTIVE);
            botUser.setApiKey(message.getText());
            botUserService.update(botUser);

            sendMessageWithButtons(botUser.getId(), getMessage("token.accepted", botUser.getLocale()), createReplyKeyboardMarkup(DEFAULT_KEYBOARD));

        } else {
            sendSimpleMessage(botUser.getId(), warningMessageText);
        }
    }

    private void handleSendingIdState(Message message, BotUser botUser) {
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
    }

    private void handleSendingNumbersState(Message message, BotUser botUser) {
        var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

        try {
            List<Phone> receivedPhones = phoneService.parseFromString(message.getText());

            List<Phone> phones = phoneService.limitPhones(receivedPhones);
            List<Phone> ignoredPhones = phoneService.getIgnoredPhones(receivedPhones);

            if (!ignoredPhones.isEmpty()) {
                final String tooManyPhonesMessage = getMessage("numbers.templates.ignored", botUser.getLocale());
                sendMessageWithButtons(botUser.getId(), tooManyPhonesMessage, replyKeyboardMarkup);
            }

            List<SentHlr> sentHlrList = hlrService.sendHlrs(phones, botUser.getApiKey());

            hlrService.getHlrInfoListAsync(sentHlrList, botUser.getApiKey()).whenComplete((result, error) -> {
                if (result != null) {
                    whenHlrInfoCompleteSuccessful(botUser.getId(), result);
                } else {
                    handleError(error, botUser);
                }
            });

        } catch (BaseException e) {
            sendMessageWithButtons(botUser.getId(), e.getMessage(), replyKeyboardMarkup);
        }

        botUser.setState(ACTIVE);
        botUserService.update(botUser);
    }

    private void whenHlrInfoCompleteSuccessful(Long telegramId, List<Hlr> result) {
        final var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

        result.stream()
            .map(hlrToTelegramResponseConverter::convert)
            .forEach(response -> sendMessageWithButtons(telegramId, response, replyKeyboardMarkup));
    }

    private void whenHlrInfoCompleteSendAnswerAsFile(BotUser botUser, FileEntity requestFile, List<Hlr> result) {
        final var replyKeyboardMarkup = createReplyKeyboardMarkup(DEFAULT_KEYBOARD);

        FileEntity responseFile = null;
        try {
            // TODO: 10/10/21 refactor later
            String responseFileName = requestFile.getReceivedFileName()
                .replaceAll("\\.xls$", ".xlsx")
                .replaceAll("\\.txt$", ".csv");

            responseFile = hlrResultFileWriterFacade.write(fileStorage.create(responseFileName), result);
            final String filename = "Result_" + responseFileName;
            sendFile(String.valueOf(botUser.getId()), responseFile.toPath(), filename);

            hlrResultFileEnricherFacade.enrich(requestFile, result).ifPresent(enrichedFile -> {
                final String enrichedFilename = "Merged_" + requestFile.getReceivedFileName();
                sendFile(String.valueOf(botUser.getId()), enrichedFile.toPath(), enrichedFilename);

                fileStorage.delete(enrichedFile.getId());
            });

        } catch (Exception e) {
            log.error("Failed to write result to the file!", e);
            sendMessageWithButtons(botUser.getId(), getMessage("error.default", botUser.getLocale()), replyKeyboardMarkup);
        } finally {
            Optional.ofNullable(responseFile)
                .map(FileEntity::getId)
                .ifPresent(fileStorage::delete);

            fileStorage.delete(requestFile.getId());
        }
    }

    private void handleNewState(Message message, BotUser botUser) {
        acceptNewToken(message, botUser, getMessage("token.required", botUser.getLocale()));
    }

    private void handleActiveState(Message message, BotUser botUser) {
        Command.fromString(message.getText()).ifPresentOrElse(command -> handleIncomeCommand(command, botUser), () -> {
            log.info("Message ({}) is not a command.", message.getText());
            String text = getMessage("warning.not-a-command", botUser.getLocale());
            sendMessageWithButtons(botUser.getId(), text, createReplyKeyboardMarkup(DEFAULT_KEYBOARD));
        });
    }

    private void handleIncomeCommand(Command command, BotUser botUser) {
        if (command == HLR) {
            final String numberForHlrMessage = getMessage("numbers.templates.required", botUser.getLocale(), limitOfNumbers);
            sendMessageWithButtons(botUser.getId(), numberForHlrMessage, createReplyKeyboardMarkup(List.of()));

            botUser.setState(SENDING_NUMBERS);
            botUserService.update(botUser);

        } else if (command == ID) {

            sendMessageWithButtons(botUser.getId(), getMessage("id.required", botUser.getLocale()), createReplyKeyboardMarkup(List.of()));

            botUser.setState(SENDING_ID);
            botUserService.update(botUser);

        } else if (command == DISCARD_STATE) {

            botUser.setState(ACTIVE);
            botUserService.update(botUser);

        } else if (command == CHANGE_API_KEY) {
            sendMessageWithButtons(botUser.getId(), getMessage("token.required", botUser.getLocale()), createReplyKeyboardMarkup(DEFAULT_KEYBOARD));

            botUser.setState(SENDING_API_KEY);
            botUserService.update(botUser);

        } else if (command == FILE) {
            sendMessageWithButtons(botUser.getId(), getMessage("file.required", botUser.getLocale()), createReplyKeyboardMarkup(DEFAULT_KEYBOARD));

            botUser.setState(SENDING_FILE);
            botUserService.update(botUser);

        } else if (command == BALANCE) {
            Balance balance = accountService.checkBalance(ApiKey.of(botUser.getApiKey()));

            sendMessageWithButtons(botUser.getId(), balance.toString(), createReplyKeyboardMarkup(DEFAULT_KEYBOARD));
        } else {
            throw new IllegalStateException("Unhandled command is present");
        }

    }

    private String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

}
