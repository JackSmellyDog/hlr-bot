package me.shaposhnik.hlrbot.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode(of = "filteredNumber")
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

}
