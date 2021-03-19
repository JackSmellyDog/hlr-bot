package me.shaposhnik.hlrbot.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class Hlr {
    private String id;
    private String number;
    private String network;
    private String status;
    
    private Ported ported;
    private Roaming roaming;

    private LocalDateTime createdAt;
    private LocalDateTime statusReceivedAt;
}
