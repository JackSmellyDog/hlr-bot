package me.shaposhnik.hlrbot.files.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class FileStorageException extends BaseException {

  public FileStorageException(Exception e) {
    super(e);
  }

  @Override
  public String getMessageKey() {
    return "exception.file-storage";
  }

}
