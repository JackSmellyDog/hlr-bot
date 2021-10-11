package me.shaposhnik.hlrbot.files.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class ReadFileException extends BaseException {
    private static final String MESSAGE = "Failed to read the file!";

    public ReadFileException(Throwable cause) {
        super(MESSAGE, cause);
    }

    public ReadFileException() {
    }
}
