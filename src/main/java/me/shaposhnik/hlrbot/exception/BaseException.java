package me.shaposhnik.hlrbot.exception;

public abstract class BaseException extends RuntimeException {

  public abstract String getMessageKey();

  protected BaseException() {
  }

  protected BaseException(String message) {
    super(message);
  }

  protected BaseException(Throwable cause) {
    super(cause);
  }
}
