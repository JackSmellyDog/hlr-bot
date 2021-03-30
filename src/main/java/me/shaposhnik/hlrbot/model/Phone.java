package me.shaposhnik.hlrbot.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Phone {
    private final String rawNumberValue;

    public static Phone of(String rawNumberValue) {
        return new Phone(rawNumberValue);
    }

    public static List<Phone> fromString(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        return Stream.of(text.split(",:\n"))
            .map(Phone::of)
            .collect(Collectors.toList());
    }

    private Phone(String rawNumberValue) {
        this.rawNumberValue = Objects.requireNonNull(rawNumberValue);
    }

    public String getFilteredNumber() {
        return rawNumberValue.replaceAll("\\D", "");
    }
}
