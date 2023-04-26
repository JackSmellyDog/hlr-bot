package me.shaposhnik.hlrbot.bot.enums;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Command {
  HLR("/hlr", "buttons.hlr.emoji", "buttons.hlr.name"),
  ID("/id", "buttons.id.emoji", "buttons.id.name"),
  FILE("/file", "buttons.file.emoji", "buttons.file.name"),
  BALANCE("/balance", "buttons.balance.emoji", "buttons.balance.name"),
  CHANGE_API_KEY("/change_api_key", "buttons.change-api-key.emoji", "buttons.change-api-key.name"),
  DISCARD_STATE("/discard_state", "buttons.discard-state.emoji", "buttons.discard-state.name");

  private final String asCommand;
  private final String emoji;
  private final String buttonAlias;

  public static Optional<Command> fromString(String text) {
    return Stream.of(values())
        .filter(value -> value.asCommand.equalsIgnoreCase(text))
        .findFirst();
  }

}
