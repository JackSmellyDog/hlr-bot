package me.shaposhnik.hlrbot.bot;

import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.FileExtensionResolver;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
public abstract class AbstractTelegramBot extends TelegramLongPollingBot {

    public void sendSimpleMessage(String chatId, String text) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();

            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Something went wrong while sending message:", e);
        }
    }

    public void sendSimpleMessage(Long chatId, String text) {
        sendSimpleMessage(Long.toString(chatId), text);
    }

    public void sendMessageWithHTML(String chatId, String html) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                .parseMode(ParseMode.HTML)
                .text(html)
                .chatId(chatId)
                .build();

            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Something went wrong while sending message with html", e);
        }
    }

    public void sendMessageWithButtons(String chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                .parseMode(ParseMode.MARKDOWN)
                .text(text)
                .chatId(chatId)
                .replyMarkup(replyKeyboardMarkup)
                .build();

            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Something went wrong while sending message with buttons", e);
        }
    }

    public void sendMessageWithButtons(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        sendMessageWithButtons(Long.toString(chatId), text, replyKeyboardMarkup);
    }

    public Optional<File> downloadDocumentToTempFile(Document document, Path directory) {
        try {
            final GetFile getFile = new GetFile();
            getFile.setFileId(document.getFileId());

            final String telegramUrlFilePath = execute(getFile).getFilePath();

            final String fileExtension =
                FileExtensionResolver.resolveExtensionOrDefaultWithDot(document.getMimeType(), ".txt");

            final File tempFile = File.createTempFile(document.getFileUniqueId(), fileExtension, directory.toFile());

            return Optional.of(downloadFile(telegramUrlFilePath, tempFile));
        } catch (TelegramApiException | IOException e) {
            log.error("Something went wrong while downloading file", e);
            return Optional.empty();
        }
    }
}
