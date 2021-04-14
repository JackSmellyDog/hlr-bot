package me.shaposhnik.hlrbot.integration.bsg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultipleHlrResponse {
    private List<HlrResponse> result;
    private String totalprice;
    private int error;
    private String currency;
}
