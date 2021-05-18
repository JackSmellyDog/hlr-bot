package me.shaposhnik.hlrbot.bot.enums;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
public enum Command {
    START("/start"),
    HLR("/hlr", "\uD83E\uDD84", "HLR"),
    ID("/id", "\uD83D\uDD0D", "ID"),
    BALANCE("/balance", "\uD83D\uDCB5", "Balance"),
    CHANGE_API_KEY("/change_api_key", "\uD83D\uDDDD", "Change Api Key"),
    DISCARD_STATE("/discard_state", "\uD83E\uDDF9", "Discard State");

    Command(String asCommand, String emoji, String buttonAlias) {
        this.emoji = emoji;
        this.asCommand = asCommand;
        this.buttonAlias = buttonAlias;
    }

    Command(String asCommand) {
        this(asCommand, "", "");
    }

    public String asButton() {
        return String.format("%s %s", emoji, buttonAlias);
    }

    private final String emoji;
    private final String asCommand;
    private final String buttonAlias;

    public static Optional<Command> fromString(String text) {

        return Stream.of(values())
            .filter(value -> value.asCommand.equalsIgnoreCase(text) || value.asButton().equalsIgnoreCase(text))
            .findFirst();
    }

}
