package me.shaposhnik.hlrbot.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class Balance {

    private String amount;
    private String currency;
    private int limit;

    private String errorDescription;
}
