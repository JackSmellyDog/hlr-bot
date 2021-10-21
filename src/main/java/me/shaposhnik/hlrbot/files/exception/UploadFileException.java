package me.shaposhnik.hlrbot.files.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class UploadFileException extends BaseException {
    private static final String MESSAGE = "Failed to upload the file!";

    public UploadFileException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
