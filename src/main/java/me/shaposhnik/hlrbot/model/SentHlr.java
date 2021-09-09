package me.shaposhnik.hlrbot.model;

import lombok.Getter;

@Getter
public class SentHlr {

    private final String id;
    private final Phone phone;

    private final String errorDescription;

    public boolean isSuccessful() {
        return id != null;
    }

    private SentHlr(String id, Phone phone, String errorDescription) {
        this.id = id;
        this.phone = phone;
        this.errorDescription = errorDescription;
    }

    public static SentHlr of(String id, Phone phone) {
        return new SentHlr(id, phone, null);
    }

    public static SentHlr asError(String errorDescription, Phone phone) {
        return new SentHlr(null, phone, errorDescription);
    }
}
