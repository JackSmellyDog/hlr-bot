package me.shaposhnik.hlrbot.integration.bsg.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MultipleHlrResponse {
    private List<HlrResponse> result;
    private String totalprice;
    private String error;
    private String currency;
}
