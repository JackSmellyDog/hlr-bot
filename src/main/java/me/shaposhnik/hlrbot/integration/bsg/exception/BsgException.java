package me.shaposhnik.hlrbot.integration.bsg.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class BsgException extends BaseException {

  @Override
  public String getMessageKey() {
    return "exception.bsg";
  }

  public BsgException() {
  }

  public BsgException(String message) {
    super(message);
  }

  public BsgException(Throwable cause) {
    super(cause);
  }
}
