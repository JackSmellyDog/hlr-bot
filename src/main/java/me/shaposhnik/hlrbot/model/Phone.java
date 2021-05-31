package me.shaposhnik.hlrbot.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class Phone {
    private static final String SEPARATORS_REGEX = "[,:;\\s]";

    private final String rawNumberValue;

    public static Phone of(String rawNumberValue) {
        return new Phone(rawNumberValue);
    }

    public static List<Phone> fromString(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        return Stream.of(text.split(SEPARATORS_REGEX))
            .filter(Objects::nonNull)
            .filter(not(String::isEmpty))
            .map(String::trim)
            .map(Phone::of)
            .distinct()
            .collect(Collectors.toList());
    }

    private Phone(String rawNumberValue) {
        this.rawNumberValue = Objects.requireNonNull(rawNumberValue);
    }

    public String getFilteredNumber() {
        return rawNumberValue.replaceAll("\\D", "");
    }

    @Override
    public String toString() {
        return rawNumberValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return Objects.equals(getFilteredNumber(), phone.getFilteredNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFilteredNumber());
    }
}
