package me.shaposhnik.hlrbot.exception;

public class ProviderException extends BaseException {
    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(Exception e) {
        super(e);
    }
}
