package me.shaposhnik.hlrbot.integration.bsg.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UnknownHlrInfoResponseException extends BsgException {
    private final String unknownResponse;

    public UnknownHlrInfoResponseException(String unknownResponse, Throwable cause) {
        super(cause);
        this.unknownResponse = unknownResponse;
    }
}
