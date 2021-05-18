package me.shaposhnik.hlrbot.model.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Ported {
    YES("Yes"),
    NO("No"),
    UNKNOWN("Unknown");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
