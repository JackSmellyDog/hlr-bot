package me.shaposhnikandrii.hlrbot.statemachine.persist;

import me.shaposhnikandrii.hlrbot.statemachine.event.HlrBotEvent;
import me.shaposhnikandrii.hlrbot.statemachine.state.HlrBotState;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HlrBotStateMachinePersist implements StateMachinePersist<HlrBotState, HlrBotEvent, Integer> {
  private final Map<Integer, StateMachineContext<HlrBotState, HlrBotEvent>> context = new ConcurrentHashMap<>();


  @Override
  public void write(StateMachineContext<HlrBotState, HlrBotEvent> stateMachineContext, Integer telegramId) {
    context.put(telegramId, stateMachineContext);
  }

  @Override
  public StateMachineContext<HlrBotState, HlrBotEvent> read(Integer telegramId) {
    return context.get(telegramId);
  }
}
