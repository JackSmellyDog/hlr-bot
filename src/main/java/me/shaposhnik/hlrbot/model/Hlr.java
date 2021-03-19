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
    
    private boolean isPorted;
    private boolean isInRoaming;

    private LocalDateTime createdAt;
    private LocalDateTime statusReceivedAt;
}
