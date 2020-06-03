package me.shaposhnikandrii.hlrbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public abstract class AbstractTelegramBot extends TelegramLongPollingBot {

  public void sendSimpleMessage(long chatId, String text) {
    try {
      SendMessage sendMessage = new SendMessage()
          .setText(text)
          .setChatId(chatId);

      execute(sendMessage);
    } catch (TelegramApiException e) {
      log.error("Something went wrong while sending message:", e);
    }
  }

  public void sendMessageWithHTML(long chatId, String html) {
    try {
      SendMessage sendMessage = new SendMessage()
          .setParseMode(ParseMode.HTML)
          .enableHtml(true)
          .setText(html)
          .setChatId(chatId);

      execute(sendMessage);
    } catch (TelegramApiException e) {
      log.error("Something went wrong while sending message with html", e);
    }
  }
}
