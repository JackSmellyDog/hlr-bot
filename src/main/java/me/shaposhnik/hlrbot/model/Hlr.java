package me.shaposhnik.hlrbot.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
public class Hlr {
    private String providerId;
    private String number;
    private String network;
    private String status;

    private Ported ported;
    private Roaming roaming;

    private ZonedDateTime createdAt;
    private ZonedDateTime statusReceivedAt;

    private Map<String, String> otherProperties;
}
