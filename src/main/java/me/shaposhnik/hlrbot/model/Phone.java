package me.shaposhnik.hlrbot.model;

import java.util.Objects;

public class Phone {

    private final String rawNumberValue;

    public static Phone of(String rawNumberValue) {
        return new Phone(rawNumberValue);
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
