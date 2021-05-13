package me.shaposhnik.hlrbot.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

import static java.time.format.DateTimeFormatter.ofPattern;

@Slf4j
public class JsonLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    private static final DateTimeFormatter MEGA_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .appendOptional(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"))
        .appendOptional(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"))
        .appendOptional(ofPattern("yyyy-MM-dd HH:mm:ss.SSSZZZZZ"))
        .appendOptional(ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        .appendOptional(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
        .appendOptional(ofPattern("yyyy-MM-dd'T'HH:mm:ss.S'Z'"))
        .appendOptional(ofPattern("yyyy-MM-dd HH:mm:ssZZZZZ"))
        .appendOptional(ofPattern("yyyy-MM-dd HH:mm:ss"))
        .appendOptional(ofPattern("yyyy-MM-dd'T'HH:mm:ssz"))
        .appendOptional(ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ"))
        .appendOptional(ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        .appendOptional(ofPattern("EEE MMM dd HH:mm:ss zzz yyyy"))
        .appendOptional(ofPattern("yyyy-MM-dd"))
        .appendOptional(ofPattern("MMMM d[d] yyyy"))
        .appendOptional(ofPattern("dd-MM-yyyy"))
        .toFormatter();

    protected JsonLocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }


    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final String text = p.getText();

        if (text == null || text.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(text, MEGA_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {

            log.warn("Failed to parse with LocalDateTime, try with LocalDate. Message: {}", e.getMessage());
            return LocalDate.parse(text, MEGA_DATE_TIME_FORMATTER).atStartOfDay();
        }

    }
}
