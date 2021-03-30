package me.shaposhnik.hlrbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.integration.bsg.BsgApiClient;
import me.shaposhnik.hlrbot.integration.bsg.dto.ApiKey;
import me.shaposhnik.hlrbot.integration.bsg.exception.BsgException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyValidationService {
    private static final String VALID_API_KEY_REGEX = "^(live|test)_[\\w]+";

    private final BsgApiClient api;

    public boolean isApiKeyValid(ApiKey apiKey) {
        if (!apiKey.getKey().matches(VALID_API_KEY_REGEX)) return false;

        try {
            api.checkBalance(apiKey);
            return true;
        } catch (BsgException e) {
            return false;
        } catch (Exception e) {
            log.error("Fail to send request to BSG to verify Api key");
            throw new RuntimeException(e);
        }
    }

}
