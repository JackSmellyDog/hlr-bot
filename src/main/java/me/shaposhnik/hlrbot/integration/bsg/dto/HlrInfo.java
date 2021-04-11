package me.shaposhnik.hlrbot.integration.bsg.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HlrInfo {
    private static final String ZONED_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssz";

    private int error;
    private String errorDescription;

    private String id;
    private String reference;
    private String msisdn;
    private String network;
    private String status;
    private Details details;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_PATTERN)
    private ZonedDateTime createdDatetime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_PATTERN)
    private ZonedDateTime statusDatetime;

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
