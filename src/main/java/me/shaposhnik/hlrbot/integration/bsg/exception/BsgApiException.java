package me.shaposhnik.hlrbot.integration.bsg.exception;

import lombok.Getter;
import me.shaposhnik.hlrbot.integration.bsg.dto.ApiError;

@Getter
public class BsgApiException extends BsgException {
    private final ApiError apiError;

    public BsgApiException(ApiError apiError) {
        super(String.format("Error code: %d. Description: %s", apiError.getError(), apiError.getErrorDescription()));
        this.apiError = apiError;
    }
}
