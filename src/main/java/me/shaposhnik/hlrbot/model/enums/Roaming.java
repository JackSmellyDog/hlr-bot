package me.shaposhnik.hlrbot.model.enums;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

@RequiredArgsConstructor
public enum Roaming {
    YES("Yes", Set.of("yes", "y", "true", "1")),
    NO("No", Set.of("no", "n", "false", "0")),
    UNKNOWN("Unknown", Set.of());

    private final String value;
    private final Set<String> aliases;

    public static Roaming fromString(String str) {
        if (str == null) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(value -> value.hasAlias(str))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return value;
    }

    private boolean hasAlias(String alias) {
        return this.aliases.contains(alias.toLowerCase());
    }
}
