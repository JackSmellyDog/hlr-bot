package me.shaposhnik.hlrbot.bot.enums;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
public enum Command {
    HLR("/hlr", "buttons.hlr.emoji", "buttons.hlr.name"),
    ID("/id", "buttons.id.emoji", "buttons.id.name"),
    FILE("/file", "buttons.file.emoji", "buttons.file.name"),
    BALANCE("/balance", "buttons.balance.emoji", "buttons.balance.name"),
    CHANGE_API_KEY("/change_api_key", "buttons.change-api-key.emoji", "buttons.change-api-key.name"),
    DISCARD_STATE("/discard_state", "buttons.discard-state.emoji", "buttons.discard-state.name");

    Command(String asCommand, String emoji, String buttonAlias) {
        this.emoji = emoji;
        this.asCommand = asCommand;
        this.buttonAlias = buttonAlias;
    }

    private final String emoji;
    private final String asCommand;
    private final String buttonAlias;

    public static Optional<Command> fromString(String text) {
        return Stream.of(values())
            .filter(value -> value.asCommand.equalsIgnoreCase(text))
            .findFirst();
    }

}
