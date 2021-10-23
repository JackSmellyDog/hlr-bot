package me.shaposhnik.hlrbot.files.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class UploadFileException extends BaseException {

    @Override
    public String getMessageKey() {
        return "exception.upload-file";
    }

    public UploadFileException(Throwable cause) {
        super(cause);
    }
}
