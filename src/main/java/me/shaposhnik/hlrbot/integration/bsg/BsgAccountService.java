package me.shaposhnik.hlrbot.integration.bsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.integration.bsg.dto.ApiKey;
import me.shaposhnik.hlrbot.integration.bsg.dto.BalanceResponse;
import me.shaposhnik.hlrbot.model.AccountBalance;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BsgAccountService {
    private final BsgApiClient api;

    public AccountBalance checkBalance(ApiKey apiKey) {
        final BalanceResponse balanceResponse = api.checkBalance(apiKey);

        return AccountBalance.builder()
            .amount(balanceResponse.getAmount())
            .currency(balanceResponse.getCurrency())
            .limit(balanceResponse.getLimit())
            .build();
    }

}
