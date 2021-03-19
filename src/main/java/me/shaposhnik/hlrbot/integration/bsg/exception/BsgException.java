package me.shaposhnik.hlrbot.integration.bsg.exception;

public class BsgException extends RuntimeException {

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
