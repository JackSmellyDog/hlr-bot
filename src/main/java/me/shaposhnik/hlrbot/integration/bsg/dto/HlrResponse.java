package me.shaposhnik.hlrbot.integration.bsg.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HlrResponse {
    private String id;
    private String reference;
    private String error;
    private String price;
    private String currency;
}
