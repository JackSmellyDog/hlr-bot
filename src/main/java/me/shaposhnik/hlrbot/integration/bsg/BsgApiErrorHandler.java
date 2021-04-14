package me.shaposhnik.hlrbot.integration.bsg;

import me.shaposhnik.hlrbot.exception.*;
import org.springframework.stereotype.Component;

import java.util.Map;

import static me.shaposhnik.hlrbot.integration.bsg.BsgApiErrorCode.*;

@Component
public class BsgApiErrorHandler {

    private final Map<BsgApiErrorCode, ? extends BaseException> errorCodeToException = Map.of(
        INVALID_API_KEY, new InvalidCredentialsException(INVALID_API_KEY.getDescription()),
        ACCOUNT_BLOCKED, new InvalidCredentialsException(ACCOUNT_BLOCKED.getDescription()),
        NOT_ENOUGH_MONEY, new LowBalanceException(NOT_ENOUGH_MONEY.getDescription()),
        HLR_NOT_FOUND, new NoSuchHlrException(HLR_NOT_FOUND.getDescription()),
        INVALID_MSISDN, new InvalidPhoneNumberException(INVALID_MSISDN.getDescription())
    );


    public void handle(BsgApiErrorCode apiErrorCode) {

        if (apiErrorCode == NO_ERRORS) {
            return;
        }

        if (errorCodeToException.containsKey(apiErrorCode)) {
            throw errorCodeToException.get(apiErrorCode);
        }

        throw new ProviderException(apiErrorCode.getDescription());
    }

}
