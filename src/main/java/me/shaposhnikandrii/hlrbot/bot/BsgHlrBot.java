package me.shaposhnikandrii.hlrbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnikandrii.hlrbot.statemachine.event.HlrBotEvent;
import me.shaposhnikandrii.hlrbot.statemachine.state.HlrBotState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class BsgHlrBot extends AbstractTelegramBot {
  private final StateMachinePersister<HlrBotState, HlrBotEvent, Integer> stateMachinePersist;
  private final StateMachineFactory<HlrBotState, HlrBotEvent> stateMachineFactory;


  @Value("${telegram-bot.name}")
  private String botUsername;

  @Value("${telegram-bot.token}")
  private String botToken;

  @SneakyThrows
  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      final Message message = update.getMessage();
      final User user = message.getFrom();


      final StateMachine<HlrBotState, HlrBotEvent> stateMachine = stateMachineFactory.getStateMachine();
      stateMachinePersist.restore(stateMachine, user.getId());


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
