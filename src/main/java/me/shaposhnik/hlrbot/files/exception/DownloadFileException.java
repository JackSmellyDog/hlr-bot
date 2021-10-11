package me.shaposhnik.hlrbot.files.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class DownloadFileException extends BaseException {
    private static final String MESSAGE = "Failed to download the file!";


    public DownloadFileException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
