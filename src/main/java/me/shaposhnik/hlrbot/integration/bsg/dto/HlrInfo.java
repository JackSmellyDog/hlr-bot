package me.shaposhnik.hlrbot.integration.bsg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class HlrInfo {
    private String id;
    private String reference;
    private String msisdn;
    private String network;
    private String status;
    private Details details;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssz")
    private ZonedDateTime createdDatetime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssz")
    private ZonedDateTime statusDatetime;

    @Getter
    @Setter
    public static class Details {
        private String ported;
        private String roaming;
    }

    @Override
    public String toString() {
        return "ID     :" + '\n' + id +
            "MSISDN :" + '\n' + msisdn +
            "NETWORK:" + '\n' + network +
            "STATUS :" + '\n' + status +
            "PORTED :" + '\n' + details.ported +
            "ROAMING:" + '\n' + details.roaming;
    }
}
