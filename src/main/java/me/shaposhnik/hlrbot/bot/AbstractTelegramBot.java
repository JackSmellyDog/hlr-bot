package me.shaposhnik.hlrbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
}
