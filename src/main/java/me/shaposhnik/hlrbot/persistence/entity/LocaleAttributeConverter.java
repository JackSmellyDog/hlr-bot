package me.shaposhnik.hlrbot.persistence.entity;

import javax.persistence.AttributeConverter;
import java.util.Locale;
import java.util.Optional;

public class LocaleAttributeConverter implements AttributeConverter<Locale, String> {

    private static final Locale DEFAULT_LOCALE = Locale.US;
    private static final String DEFAULT_LANGUAGE = "en";

    @Override
    public String convertToDatabaseColumn(Locale locale) {
        return Optional.ofNullable(locale.getLanguage()).orElse(DEFAULT_LANGUAGE);
    }

    @Override
    public Locale convertToEntityAttribute(String s) {
        return Optional.ofNullable(s)
                .map(Locale::forLanguageTag)
                .orElse(DEFAULT_LOCALE);
    }

}
