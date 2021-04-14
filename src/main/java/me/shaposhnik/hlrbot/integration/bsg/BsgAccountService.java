package me.shaposhnik.hlrbot.integration.bsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.exception.ProviderException;
import me.shaposhnik.hlrbot.integration.bsg.dto.ApiKey;
import me.shaposhnik.hlrbot.integration.bsg.dto.BalanceResponse;
import me.shaposhnik.hlrbot.model.Balance;
import org.springframework.stereotype.Service;

import static me.shaposhnik.hlrbot.integration.bsg.BsgApiErrorCode.INVALID_API_KEY;
import static me.shaposhnik.hlrbot.integration.bsg.BsgApiErrorCode.fromErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class BsgAccountService {
    private static final String VALID_API_KEY_REGEX = "^(live|test)_[\\w]+";

    private final BsgApiClient api;
    private final BsgApiErrorHandler bsgApiErrorHandler;

    public Balance checkBalance(ApiKey apiKey) {
        final BalanceResponse balanceResponse = api.checkBalance(apiKey);

        bsgApiErrorHandler.handle(fromErrorCode(balanceResponse.getError()));

        return Balance.builder()
            .amount(balanceResponse.getAmount())
            .currency(balanceResponse.getCurrency())
            .limit(balanceResponse.getLimit())
            .build();
    }

    public boolean isApiKeyValid(ApiKey apiKey) {
        if (!apiKey.getKey().matches(VALID_API_KEY_REGEX)) return false;

        try {
            return apiKey.getKey().matches(VALID_API_KEY_REGEX)
                && fromErrorCode(api.checkBalance(apiKey).getError()) != INVALID_API_KEY;
        } catch (Exception e) {
            log.error("Fail to send request to BSG to verify Api key");
            throw new ProviderException(e);
        }
    }

}
