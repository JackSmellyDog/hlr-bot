package me.shaposhnik.hlrbot.bot.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum Command {
    START("/start"),
    HLR("/hlr"),
    STATUS("/status"),
    STOP("/stop");

    private final String telegramCommand;

    public static Optional<Command> fromString(String text) {
        return Stream.of(values())
            .filter(value -> value.telegramCommand.equalsIgnoreCase(text))
            .findFirst();
    }
}
