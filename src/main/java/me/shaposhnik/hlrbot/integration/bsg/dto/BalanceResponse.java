package me.shaposhnik.hlrbot.integration.bsg.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BalanceResponse {
    private Double amount;
    private String currency;
    private int limit;
}
