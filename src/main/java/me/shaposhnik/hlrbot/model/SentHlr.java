package me.shaposhnik.hlrbot.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SentHlr {

    private final String id;
    private final Phone phone;

    private final String errorDescription;

    public boolean isSuccessful() {
        return id != null;
    }

    public static SentHlr of(String id, Phone phone) {
        return new SentHlr(id, phone, null);
    }

    public static SentHlr asError(String errorDescription, Phone phone) {
        return new SentHlr(null, phone, errorDescription);
    }
}
