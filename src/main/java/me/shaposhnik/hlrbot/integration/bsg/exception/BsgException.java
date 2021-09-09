package me.shaposhnik.hlrbot.integration.bsg.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class BsgException extends BaseException {

    public BsgException() {
    }

    public BsgException(String message) {
        super(message);
    }

    public BsgException(String message, Throwable cause) {
        super(message, cause);
    }

    public BsgException(Throwable cause) {
        super(cause);
    }
}
