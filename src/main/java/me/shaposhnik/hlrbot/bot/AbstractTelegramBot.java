package me.shaposhnik.hlrbot.bot;

import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.exception.DownloadFileException;
import me.shaposhnik.hlrbot.files.exception.UploadFileException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.nio.file.Path;

@Slf4j
public abstract class AbstractTelegramBot extends TelegramLongPollingBot {

    public void sendSimpleMessage(String chatId, String text) {
        var sendMessage = SendMessage.builder()
            .text(text)
            .chatId(chatId)
            .build();

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Something went wrong while sending message:", e);
        }
    }

    public void sendSimpleMessage(Long chatId, String text) {
        sendSimpleMessage(Long.toString(chatId), text);
    }

    public void sendMessageWithHTML(String chatId, String html) {
        var sendMessage = SendMessage.builder()
            .parseMode(ParseMode.HTML)
            .text(html)
            .chatId(chatId)
            .build();

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Something went wrong while sending message with html", e);
        }
    }

    public void sendMessageWithButtons(String chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        var sendMessage = SendMessage.builder()
            .parseMode(ParseMode.MARKDOWN)
            .text(text)
            .chatId(chatId)
            .replyMarkup(replyKeyboardMarkup)
            .build();

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Something went wrong while sending message with buttons", e);
        }
    }

    public void sendMessageWithButtons(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        sendMessageWithButtons(Long.toString(chatId), text, replyKeyboardMarkup);
    }

    private String getTelegramUrlFilePath(String fileId) {
        final GetFile getFile = new GetFile();
        getFile.setFileId(fileId);

        try {
            return execute(getFile).getFilePath();
        } catch (TelegramApiException e) {
            log.error("Something went wrong while getting a file path in Telegram", e);
            throw new DownloadFileException(e);
        }
    }

    protected Path downloadFile(String fileId, Path pathToFile) {
        try {
            final String telegramUrlFilePath = getTelegramUrlFilePath(fileId);

            return downloadFile(telegramUrlFilePath, pathToFile.toFile()).toPath();
        } catch (TelegramApiException e) {
            log.error("Failed to download file!", e);
            throw new DownloadFileException(e);
        }
    }

    protected InputStream downloadFileAsInputStream(String fileId) {
        try {
            final String telegramUrlFilePath = getTelegramUrlFilePath(fileId);

            return downloadFileAsStream(telegramUrlFilePath);
        } catch (TelegramApiException e) {
            log.error("Failed to download file!", e);
            throw new DownloadFileException(e);
        }
    }

    protected void sendFile(String chatId, Path pathToFile, String fileName) {
        var inputFile = new InputFile()
            .setMedia(pathToFile.toFile(), fileName);

        var sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(inputFile);

        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Something went wrong while uploading a file to Telegram", e);
            throw new UploadFileException(e);
        }
    }
}
