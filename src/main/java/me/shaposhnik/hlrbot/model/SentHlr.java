package me.shaposhnik.hlrbot.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class SentHlr {

    private final String id;
    private final Phone phone;
    private final String reference;

    private final String errorDescription;

    public boolean isSuccessful() {
        return id != null;
    }

    public static SentHlr of(String id, Phone phone, String reference) {
        return new SentHlr(id, phone, reference, null);
    }

    public static SentHlr asError(String errorDescription, Phone phone, String reference) {
        return new SentHlr(null, phone, reference, errorDescription);
    }

    public static SentHlr asError(String errorDescription, Phone phone) {
        return new SentHlr(null, phone, null, errorDescription);
    }
}
