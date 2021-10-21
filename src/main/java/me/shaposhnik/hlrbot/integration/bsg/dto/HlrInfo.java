package me.shaposhnik.hlrbot.integration.bsg.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.shaposhnik.hlrbot.util.JsonLocalDateTimeDeserializer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HlrInfo {

    private int error;
    private String errorDescription;

    private String id;
    private String reference;
    private String msisdn;
    private String network;
    private String status;
    private Details details;

    @JsonDeserialize(using = JsonLocalDateTimeDeserializer.class)
    private LocalDateTime createdDatetime;

    @JsonDeserialize(using = JsonLocalDateTimeDeserializer.class)
    private LocalDateTime statusDatetime;

    @Getter(onMethod_ = {@JsonAnyGetter})
    @Setter(AccessLevel.NONE)
    private Map<String, String> otherProperties = new HashMap<>();

    @JsonAnySetter
    public void add(String key, String value) {
        otherProperties.put(key, value);
    }

    @Getter
    @Setter
    public static class Details {
        private String ported;
        private String roaming;

        @Getter(onMethod_ = {@JsonAnyGetter})
        @Setter(AccessLevel.NONE)
        private Map<String, String> otherDetails = new HashMap<>();

        @JsonAnySetter
        public void add(String key, String value) {
            otherDetails.put(key, value);
        }
    }

}
