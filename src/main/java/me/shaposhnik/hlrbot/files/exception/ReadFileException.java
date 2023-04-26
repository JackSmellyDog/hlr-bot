package me.shaposhnik.hlrbot.files.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class ReadFileException extends BaseException {

  public ReadFileException(Throwable cause) {
    super(cause);
  }

  public ReadFileException() {
  }

  @Override
  public String getMessageKey() {
    return "exception.read-file";
  }

}
