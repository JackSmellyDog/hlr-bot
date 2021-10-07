package me.shaposhnik.hlrbot.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Phone {

    private final String rawNumberValue;
    private final String filteredNumber;

    public static Phone of(String rawNumberValue) {
        return new Phone(rawNumberValue);
    }

    private Phone(String rawNumberValue) {
        this.rawNumberValue = Objects.requireNonNull(rawNumberValue);
        this.filteredNumber = rawNumberValue.replaceAll("\\D", "");
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
        return Objects.equals(filteredNumber, phone.getFilteredNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(filteredNumber);
    }
}
