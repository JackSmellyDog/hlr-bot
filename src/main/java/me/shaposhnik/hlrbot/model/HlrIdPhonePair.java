package me.shaposhnik.hlrbot.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class HlrIdPhonePair {
    private final HlrId hlrId;
    private final Phone phone;

    public String getPlainId() {
        return hlrId.getId();
    }

    public String getFilteredNumber() {
        return phone.getFilteredNumber();
    }

    public Phone getPhone() {
        return phone;
    }
}
