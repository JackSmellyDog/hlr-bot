package me.shaposhnikandrii.hlrbot.config;

import lombok.RequiredArgsConstructor;
import me.shaposhnikandrii.hlrbot.statemachine.event.HlrBotEvent;
import me.shaposhnikandrii.hlrbot.statemachine.persist.HlrBotStateMachinePersist;
import me.shaposhnikandrii.hlrbot.statemachine.state.HlrBotState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.EnumSet;

import static me.shaposhnikandrii.hlrbot.statemachine.state.HlrBotState.*;

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class HlrBotStateMachineConfig extends EnumStateMachineConfigurerAdapter<HlrBotState, HlrBotEvent> {


  @Bean
  public StateMachinePersister<HlrBotState, HlrBotEvent, Integer> persist() {
    return new DefaultStateMachinePersister<>(new HlrBotStateMachinePersist());
  }


  @Override
  public void configure(StateMachineConfigurationConfigurer<HlrBotState, HlrBotEvent> config) throws Exception {
    config
        .withConfiguration()
        .autoStartup(true);
  }

  @Override
  public void configure(StateMachineStateConfigurer<HlrBotState, HlrBotEvent> states) throws Exception {
    states
        .withStates()
        .initial(WAIT_FOR_COMMAND)
        .end(COMMAND_IS_DONE)
        .states(EnumSet.allOf(HlrBotState.class));
  }

  @Override
  public void configure(StateMachineTransitionConfigurer<HlrBotState, HlrBotEvent> transitions) throws Exception {
    transitions
        .withExternal()
        .source(WAIT_FOR_COMMAND)
        .target(WAIT_FOR_NUMBERS)
        .action(context -> {
          System.out.println("Fuck!");
        });
  }

}