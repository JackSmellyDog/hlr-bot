package me.shaposhnik.hlrbot.files.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class WriteFileException extends BaseException {
    private static final String MESSAGE = "Failed to write the file!";

    public WriteFileException(Throwable cause) {
        super(MESSAGE, cause);
    }

    public WriteFileException() {
    }
}
