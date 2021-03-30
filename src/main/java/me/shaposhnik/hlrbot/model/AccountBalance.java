package me.shaposhnik.hlrbot.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccountBalance {
    private Double amount;
    private String currency;
    private int limit;

    @Override
    public String toString() {
        return "Amount:" + ' ' + amount + '\n' +
            "Currency:" + ' ' + currency + '\n' +
            "Limit:" + ' ' + limit;
    }
}
