package me.shaposhnik.hlrbot.service;

import me.shaposhnik.hlrbot.model.Phone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
public class PhoneService {

    private static final String SEPARATORS_REGEX = "[,:;\\s]";

    @Value("${bot.limit-of-numbers}")
    private int limitOfNumbers;

    public List<Phone> parseFromString(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        return toPhones(Arrays.asList(text.split(SEPARATORS_REGEX)));
    }

    public List<Phone> toPhones(List<String> plainPhones) {
        return plainPhones.stream()
                .filter(Objects::nonNull)
                .filter(not(String::isEmpty))
                .map(String::trim)
                .map(Phone::of)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Phone> limitPhones(List<Phone> phones) {
        if (phones.size() > limitOfNumbers) {
            return List.copyOf(phones.subList(0, limitOfNumbers));
        }

        return phones;
    }

    public List<Phone> getIgnoredPhones(List<Phone> phones) {
        if (phones.size() > limitOfNumbers) {
            return List.copyOf(phones.subList(limitOfNumbers, phones.size()));
        }

        return List.of();
    }
}
