package me.shaposhnik.hlrbot.files.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class WriteFileException extends BaseException {

  public WriteFileException(Throwable cause) {
    super(cause);
  }

  @Override
  public String getMessageKey() {
    return "exception.write-file";
  }

  public WriteFileException() {
  }
}
